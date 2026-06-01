package com.mohamed.pdfreader.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.mohamed.pdfreader.domain.model.PdfFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object StorageUtils {
    suspend fun getAllPdfs(context: Context): List<PdfFile> = withContext(Dispatchers.IO) {
        val pdfList = mutableListOf<PdfFile>()
        val collection = MediaStore.Files.getContentUri("external")
        
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA
        )

        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("application/pdf")
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown.pdf"
                val size = cursor.getLong(sizeColumn)
                val date = cursor.getLong(dateColumn)
                val path = cursor.getString(dataColumn) ?: ""
                
                // التأكد من أن الملف موجود فعلياً وليس مجرد كاش قديم
                if (File(path).exists()) {
                    val contentUri = Uri.withAppendedPath(collection, id.toString())
                    pdfList.add(PdfFile(id, name, contentUri, size, date, path))
                }
            }
        }
        return@withContext pdfList
    }
}
