package com.mohamed.pdfreader.pdfcore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfRendererCore(context: Context, uri: Uri) {
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null

    init {
        try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            parcelFileDescriptor?.let {
                pdfRenderer = PdfRenderer(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val pageCount: Int
        get() = pdfRenderer?.pageCount ?: 0

    suspend fun renderPage(pageIndex: Int, width: Int): Bitmap? = withContext(Dispatchers.IO) {
        if (pdfRenderer == null || pageIndex < 0 || pageIndex >= pageCount) return@withContext null

        return@withContext try {
            val page = pdfRenderer!!.openPage(pageIndex)
            
            // حساب الطول بناءً على العرض للحفاظ على الأبعاد
            val ratio = page.height.toFloat() / page.width.toFloat()
            val height = (width * ratio).toInt()

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            
            // جعل الخلفية بيضاء (لأن الـ PDF الشفاف سيظهر بظلال سوداء)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun close() {
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
    }
}
