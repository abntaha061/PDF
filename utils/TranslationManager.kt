package com.mohamed.pdfreader.utils

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class TranslationManager {
    
    // إعداد المترجم من الألماني للعربي
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.GERMAN)
        .setTargetLanguage(TranslateLanguage.ARABIC)
        .build()

    private val germanArabicTranslator = Translation.getClient(options)

    suspend fun downloadModelsIfNeeded(): Boolean {
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        return try {
            germanArabicTranslator.downloadModelIfNeeded(conditions).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun translate(text: String): String {
        return try {
            germanArabicTranslator.translate(text).await()
        } catch (e: Exception) {
            e.printStackTrace()
            "خطأ في الترجمة، تأكد من تحميل القاموس"
        }
    }

    fun close() {
        germanArabicTranslator.close()
    }
}
