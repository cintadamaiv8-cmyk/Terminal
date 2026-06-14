package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast

object FileExportUtils {
    fun savePythonFile(context: Context, fileName: String, content: String) {
        try {
            val name = if (fileName.endsWith(".py")) fileName else "$fileName.py"
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, "text/x-python")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                resolver.openOutputStream(it)?.use { stream ->
                    stream.write(content.toByteArray())
                }
                Toast.makeText(context, "Saved to Downloads: $name", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareScript(context: Context, content: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_SUBJECT, "Python Script")
        }
        context.startActivity(
            Intent.createChooser(intent, "Share Python Script")
        )
    }
}
