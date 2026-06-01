package com.mohamed.pdfreader.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import com.mohamed.pdfreader.data.local.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TtsManager(
    context: Context, 
    private val settingsManager: SettingsManager
) : TextToSpeech.OnInitListener {
    
    private var tts: TextToSpeech? = null
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
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
            // جلب السرعة من الإعدادات التي اختارها المستخدم
            tts?.setSpeechRate(settingsManager.ttsSpeed.value)
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
