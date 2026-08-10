package com.isankamil.mcjobid.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object FileUtil {
    
    /**
     * Copies an image from a URI (e.g. content://) to the app's internal storage
     * to ensure persistent access across screens and sessions.
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return uri.toString()

            val newFileName = "profile_${System.currentTimeMillis()}.jpg"
            val newFile = File(context.filesDir, newFileName)

            FileOutputStream(newFile).use { output ->
                inputStream.use { input ->
                    input.copyTo(output)
                }
            }

            if (newFile.exists() && newFile.length() > 0) {
                // Delete previous profile images to free space
                context.filesDir.listFiles { _, name -> name.startsWith("profile_") && name != newFileName }?.forEach { 
                    try { it.delete() } catch (_: Exception) {}
                }
                Uri.fromFile(newFile).toString()
            } else {
                uri.toString()
            }
        } catch (e: Exception) {
            android.util.Log.e("FileUtil", "Error saving image: ${e.message}")
            uri.toString()
        }
    }
}
