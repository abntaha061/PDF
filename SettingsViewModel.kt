package com.mohamed.pdfreader.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.mohamed.pdfreader.data.local.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsManager: SettingsManager
) : ViewModel() {
    
    fun updateTheme(isDark: Boolean) {
        settingsManager.setDarkMode(isDark)
    }

    fun updateTtsSpeed(speed: Float) {
        settingsManager.setTtsSpeed(speed)
    }
}
