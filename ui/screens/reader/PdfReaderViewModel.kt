package com.mohamed.pdfreader.ui.screens.reader

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamed.pdfreader.utils.OcrManager
import com.mohamed.pdfreader.utils.TranslationManager
import com.mohamed.pdfreader.utils.TtsManager
import com.mohamed.pdfreader.utils.WordBox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfReaderViewModel @Inject constructor(
    private val ttsManager: TtsManager,
    private val translationManager: TranslationManager,
    private val ocrManager: OcrManager
) : ViewModel() {

    // قائمة الكلمات المستخرجة من الصفحة الحالية
    private val _currentWords = MutableStateFlow<List<WordBox>>(emptyList())
    val currentWords: StateFlow<List<WordBox>> = _currentWords

    // الكلمة التي ضغط عليها المستخدم
    private val _selectedWord = MutableStateFlow<String?>(null)
    val selectedWord: StateFlow<String?> = _selectedWord

    // نتيجة الترجمة
    private val _translationResult = MutableStateFlow<String?>(null)
    val translationResult: StateFlow<String?> = _translationResult

    // حالة التحميل (قاموس الترجمة أو معالجة الصفحة)
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    init {
        // بمجرد فتح الشاشة، نبدأ بتحميل نموذج الترجمة الألماني-العربي لو لم يكن موجوداً
        viewModelScope.launch(Dispatchers.IO) {
            translationManager.downloadModelsIfNeeded()
        }
    }

    fun processPageForOcr(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            _isProcessing.value = true
            val words = ocrManager.extractWordsFromBitmap(bitmap)
            _currentWords.value = words
            _isProcessing.value = false
        }
    }

    fun onWordTapped(word: String) {
        _selectedWord.value = word
        _translationResult.value = "جاري الترجمة..."
        
        viewModelScope.launch(Dispatchers.IO) {
            val result = translationManager.translate(word)
            _translationResult.value = result
        }
    }

    fun speakWord(word: String) {
        ttsManager.speak(word, isGerman = true)
    }

    fun dismissTranslationDialog() {
        _selectedWord.value = null
        _translationResult.value = null
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        translationManager.close()
    }
}
