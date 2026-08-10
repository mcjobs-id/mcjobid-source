package com.isankamil.mcjobid.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Helper untuk upload foto profil ke ImgBB (gratis, tidak butuh Firebase Storage).
 * Dilengkapi kompresi otomatis (max 512x512 px, JPEG 80%) agar hemat kuota & upload super cepat.
 */
object FirebaseStorageHelper {

    private const val IMGBB_API_KEY = "95f55924917c2e610d3eb410fb1c3edc"
    private const val IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload"
    private const val TAG = "FirebaseStorageHelper"
    private const val MAX_AVATAR_DIMEN = 512

    /**
     * Upload foto profil ke ImgBB dengan kompresi maksimal.
     *
     * @param context   Android context (untuk membaca file lokal)
     * @param userId    UID pengguna (dipakai sebagai nama file di ImgBB)
     * @param sourceUri URI sumber foto (content://, file://, atau https://)
     * @return URL download publik (https://i.ibb.co/...) jika berhasil, null jika gagal
     */
    suspend fun uploadProfilePhoto(
        context: Context,
        userId: String,
        sourceUri: String
    ): String? = withContext(Dispatchers.IO) {
        if (sourceUri.isBlank()) return@withContext null

        // Jika sudah berupa HTTPS URL (sudah di-upload sebelumnya), langsung kembalikan
        if (sourceUri.startsWith("https://")) return@withContext sourceUri

        return@withContext try {
            val uri = Uri.parse(sourceUri)
            val bytes = compressImageFromUri(context, uri) ?: run {
                Log.e(TAG, "Gagal kompresi/membaca file dari URI: $sourceUri")
                return@withContext null
            }

            // Encode ke Base64
            val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)

            // Upload ke ImgBB via HTTP POST
            uploadToImgBB(base64Image, "profile_$userId")
        } catch (e: Exception) {
            Log.e(TAG, "Upload gagal: ${e.message}", e)
            null
        }
    }

    /**
     * Membaca dan kompresi gambar dari URI menjadi ByteArray (JPEG, max 512x512 px, quality 80%).
     * Memangkas ukuran foto kamera (5MB-10MB) menjadi sangat ringan (~50KB-120KB).
     */
    private fun compressImageFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // 1. Decode ukuran asli gambar
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            if (options.outWidth <= 0 || options.outHeight <= 0) return null

            // 2. Hitung rasio kompresi (sample size) agar dimensi max 512px
            var sampleSize = 1
            while (options.outWidth / sampleSize > MAX_AVATAR_DIMEN || options.outHeight / sampleSize > MAX_AVATAR_DIMEN) {
                sampleSize *= 2
            }

            // 3. Decode ulang gambar dengan sampel terkompresi
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val stream2 = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(stream2, null, decodeOptions)
            stream2.close()

            if (bitmap == null) return null

            // 4. Kompresi ke format JPEG kualitas 80%
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            bitmap.recycle()

            val compressedBytes = outputStream.toByteArray()
            Log.i(TAG, "Foto terkompresi dari ${options.outWidth}x${options.outHeight} -> ukuran byte: ${compressedBytes.size / 1024} KB")
            compressedBytes
        } catch (e: Exception) {
            Log.e(TAG, "Error kompresi gambar, mencoba fallback raw bytes: ${e.message}")
            readBytesFromUri(context, uri)
        }
    }

    /**
     * Mengirim gambar Base64 ke ImgBB API dan mengembalikan URL publik.
     */
    private fun uploadToImgBB(base64Image: String, imageName: String): String? {
        return try {
            val url = URL(IMGBB_UPLOAD_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connectTimeout = 30_000
                readTimeout = 30_000
            }

            val postData = buildString {
                append("key=").append(IMGBB_API_KEY)
                append("&name=").append(imageName)
                append("&image=").append(java.net.URLEncoder.encode(base64Image, "UTF-8"))
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(postData)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val success = json.getBoolean("success")
                if (success) {
                    val displayUrl = json.getJSONObject("data").getString("display_url")
                    Log.i(TAG, "Upload berhasil: $displayUrl")
                    displayUrl
                } else {
                    Log.e(TAG, "ImgBB response tidak sukses: $response")
                    null
                }
            } else {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "HTTP $responseCode: $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadToImgBB error: ${e.message}", e)
            null
        }
    }

    /**
     * Fallback membaca bytes mentah jika kompresi mengalami masalah.
     */
    private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            when {
                uri.scheme == "content" -> {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }
                uri.scheme == "file" -> {
                    val file = File(uri.path ?: return null)
                    if (file.exists()) file.readBytes() else null
                }
                uri.path?.startsWith("/") == true -> {
                    val file = File(uri.path!!)
                    if (file.exists()) file.readBytes() else null
                }
                else -> {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal membaca URI: ${e.message}")
            null
        }
    }
}
