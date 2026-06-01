package com.mohamed.pdfreader.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // تعيين اللغة الألمانية كافتراضية لأنها الأهم في مشروعك
            val result = tts?.setLanguage(Locale.GERMAN)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                _isInitialized.value = true
            }
        }
    }

    fun speak(text: String, isGerman: Boolean = true) {
        if (_isInitialized.value) {
            val locale = if (isGerman) Locale.GERMAN else Locale("ar")
            tts?.language = locale
            // تقليل سرعة النطق قليلاً لتسهيل التعلم (0.8 بدلاً من 1.0)
            tts?.setSpeechRate(0.8f)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
