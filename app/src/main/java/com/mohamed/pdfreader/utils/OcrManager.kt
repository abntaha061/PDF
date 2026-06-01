package com.mohamed.pdfreader.utils

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class WordBox(
    val text: String,
    val boundingBox: Rect
)

class OcrManager {
    // نستخدم الـ Latin model لأنه يدعم الألمانية والإنجليزية
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractWordsFromBitmap(bitmap: Bitmap): List<WordBox> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()
            
            val wordsList = mutableListOf<WordBox>()
            // استخراج كل كلمة بإحداثياتها
            for (block in result.textBlocks) {
                for (line in block.lines) {
                    for (element in line.elements) {
                        element.boundingBox?.let { rect ->
                            wordsList.add(WordBox(text = element.text, boundingBox = rect))
                        }
                    }
                }
            }
            wordsList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
