package com.isankamil.mcjobid.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.isankamil.mcjobid.BuildConfig
import com.isankamil.mcjobid.domain.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateManager @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "AppUpdateManager"
        private const val CONFIG_COLLECTION = "app_config"
        private const val UPDATE_DOC = "update_info"
    }

    /**
     * Mengamati informasi pembaruan aplikasi secara real-time dari Firestore.
     * Document: `app_config/update_info`
     */
    fun observeUpdateInfo(): Flow<AppUpdateInfo?> = callbackFlow {
        val listener = firestore.collection(CONFIG_COLLECTION).document(UPDATE_DOC)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Gagal memuat update info dari Firestore: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val latestCode = (snapshot.getLong("latestVersionCode") ?: 0L).toInt()
                    val latestName = snapshot.getString("latestVersionName") ?: ""
                    val minSupported = (snapshot.getLong("minSupportedVersionCode") ?: 0L).toInt()
                    val downloadUrl = snapshot.getString("apkDownloadUrl") ?: ""
                    val sizeMb = snapshot.getString("apkSizeMb") ?: ""
                    val notes = snapshot.getString("releaseNotes") ?: ""
                    val isForce = snapshot.getBoolean("isForceUpdate") ?: false
                    val releaseDate = snapshot.getString("releaseDate") ?: ""

                    val currentCode = BuildConfig.VERSION_CODE
                    val isUpdateAvailable = latestCode > currentCode && downloadUrl.isNotBlank()
                    val isForced = isUpdateAvailable && (isForce || currentCode < minSupported)

                    val info = AppUpdateInfo(
                        latestVersionCode = latestCode,
                        latestVersionName = latestName,
                        minSupportedVersionCode = minSupported,
                        apkDownloadUrl = downloadUrl,
                        apkSizeMb = sizeMb,
                        releaseNotes = notes,
                        isForceUpdate = isForce,
                        releaseDate = releaseDate,
                        isUpdateAvailable = isUpdateAvailable,
                        isForced = isForced
                    )
                    trySend(info)
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    /**
     * Mempublikasikan / Memperbarui info rilis di Firestore (Hanya untuk Developer).
     */
    suspend fun publishReleaseInfo(info: AppUpdateInfo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = hashMapOf(
                "latestVersionCode" to info.latestVersionCode,
                "latestVersionName" to info.latestVersionName,
                "minSupportedVersionCode" to info.minSupportedVersionCode,
                "apkDownloadUrl" to info.apkDownloadUrl,
                "apkSizeMb" to info.apkSizeMb,
                "releaseNotes" to info.releaseNotes,
                "isForceUpdate" to info.isForceUpdate,
                "releaseDate" to info.releaseDate
            )
            firestore.collection(CONFIG_COLLECTION).document(UPDATE_DOC).set(data, SetOptions.merge())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengunduh file APK langsung dengan progress bar lalu memicu intent instalasi Android.
     */
    suspend fun downloadAndInstallApk(
        context: Context,
        updateInfo: AppUpdateInfo,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        var input: InputStream? = null
        var output: FileOutputStream? = null
        var connection: HttpURLConnection? = null

        try {
            val downloadUrl = updateInfo.apkDownloadUrl
            if (downloadUrl.isBlank()) {
                withContext(Dispatchers.Main) { onError("Tautan unduhan APK tidak valid.") }
                return@withContext
            }

            val url = URL(downloadUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                withContext(Dispatchers.Main) {
                    onError("Gagal mengunduh file APK: Server merespon ${connection.responseCode}")
                }
                return@withContext
            }

            val fileLength = connection.contentLength
            val cacheDir = context.getExternalFilesDir("updates") ?: context.cacheDir
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val apkFile = File(cacheDir, "mcjobid_v${updateInfo.latestVersionName}.apk")
            if (apkFile.exists()) apkFile.delete()

            input = connection.inputStream
            output = FileOutputStream(apkFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            var lastProgress = 0

            while (input.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    val progress = ((total * 100) / fileLength).toInt()
                    if (progress != lastProgress) {
                        lastProgress = progress
                        withContext(Dispatchers.Main) { onProgress(progress) }
                    }
                }
                output.write(data, 0, count)
            }

            output.flush()

            withContext(Dispatchers.Main) {
                onComplete()
                triggerApkInstall(context, apkFile)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error saat download & install APK: ${e.message}", e)
            withContext(Dispatchers.Main) {
                onError("Gagal mengunduh pembaruan: ${e.localizedMessage ?: "Koneksi terputus"}")
            }
        } finally {
            try {
                output?.close()
                input?.close()
                connection?.disconnect()
            } catch (_: Exception) {}
        }
    }

    /**
     * Memicu dialog instalasi bawaan sistem Android menggunakan FileProvider.
     */
    fun triggerApkInstall(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                Log.e(TAG, "File APK tidak ditemukan di ${apkFile.absolutePath}")
                return
            }

            // Periksa izin Install Unknown Apps di Android 8.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val manageIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(manageIntent)
                    return
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memicu instalasi APK: ${e.message}", e)
        }
    }
}
