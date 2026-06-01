package com.mohamed.pdfreader.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pdf_settings", Context.MODE_PRIVATE)

    // الثيم الافتراضي داكن كما طلبت مسبقاً
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    // سرعة النطق الافتراضية 0.8 للمساعدة في التعلم
    private val _ttsSpeed = MutableStateFlow(prefs.getFloat("tts_speed", 0.8f))
    val ttsSpeed: StateFlow<Float> = _ttsSpeed

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("dark_mode", isDark).apply()
        _isDarkMode.value = isDark
    }

    fun setTtsSpeed(speed: Float) {
        prefs.edit().putFloat("tts_speed", speed).apply()
        _ttsSpeed.value = speed
    }
}
