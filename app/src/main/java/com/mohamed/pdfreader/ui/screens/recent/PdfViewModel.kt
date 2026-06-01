package com.mohamed.pdfreader.ui.screens.reader

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamed.pdfreader.data.local.BookmarkDao
import com.mohamed.pdfreader.domain.model.BookmarkEntity
import com.mohamed.pdfreader.domain.model.DrawPath
import com.mohamed.pdfreader.utils.OcrManager
import com.mohamed.pdfreader.utils.TranslationManager
import com.mohamed.pdfreader.utils.TtsManager
import com.mohamed.pdfreader.utils.WordBox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfReaderViewModel @Inject constructor(
    private val ttsManager: TtsManager,
    private val translationManager: TranslationManager,
    private val ocrManager: OcrManager,
    private val bookmarkDao: BookmarkDao
) : ViewModel() {

    private val _currentWords = MutableStateFlow<List<WordBox>>(emptyList())
    val currentWords: StateFlow<List<WordBox>> = _currentWords

    private val _selectedWord = MutableStateFlow<String?>(null)
    val selectedWord: StateFlow<String?> = _selectedWord

    private val _translationResult = MutableStateFlow<String?>(null)
    val translationResult: StateFlow<String?> = _translationResult

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _showJumpDialog = MutableStateFlow(false)
    val showJumpDialog: StateFlow<Boolean> = _showJumpDialog

    // حفظ الرسومات لكل صفحة (رقم الصفحة -> قائمة الخطوط)
    private val _pageDrawings = MutableStateFlow<Map<Int, List<DrawPath>>>(emptyMap())
    val pageDrawings: StateFlow<Map<Int, List<DrawPath>>> = _pageDrawings

    // حالة التلخيص بالذكاء الاصطناعي
    private val _summaryResult = MutableStateFlow<String?>(null)
    val summaryResult: StateFlow<String?> = _summaryResult

    init {
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

    fun dismissSummaryDialog() {
        _summaryResult.value = null
    }

    fun addBookmark(uri: String, pageIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val bookmark = BookmarkEntity(
                fileUri = uri,
                pageIndex = pageIndex,
                title = "تم الحفظ عند صفحة ${pageIndex + 1}",
                timestamp = System.currentTimeMillis()
            )
            bookmarkDao.insertBookmark(bookmark)
        }
    }

    fun setShowJumpDialog(show: Boolean) {
        _showJumpDialog.value = show
    }

    // إضافة خط رسم جديد لصفحة معينة
    fun addDrawingToPage(pageIndex: Int, path: DrawPath) {
        val currentMap = _pageDrawings.value.toMutableMap()
        val currentPaths = currentMap[pageIndex]?.toMutableList() ?: mutableListOf()
        currentPaths.add(path)
        currentMap[pageIndex] = currentPaths
        _pageDrawings.value = currentMap
    }

    // مسح رسومات الصفحة الحالية
    fun clearDrawings(pageIndex: Int) {
        val currentMap = _pageDrawings.value.toMutableMap()
        currentMap[pageIndex] = emptyList()
        _pageDrawings.value = currentMap
    }

    // توليد تلخيص للصفحة الحالية بناءً على النص المستخرج
    fun summarizeCurrentPage() {
        _summaryResult.value = "جاري قراءة الصفحة وتلخيصها..."
        viewModelScope.launch(Dispatchers.IO) {
            val words = _currentWords.value
            if (words.isEmpty()) {
                _summaryResult.value = "عذراً، لم أتمكن من العثور على نص في هذه الصفحة لتلخيصه."
                return@launch
            }
            
            // تجميع النص
            val fullText = words.joinToString(" ") { it.text }
            
            // محاكاة استدعاء API خارجي للتلخيص (هنا يمكنك لاحقاً إضافة Gemini API أو ML Kit للملخصات)
            delay(1500)
            
            _summaryResult.value = "✨ ملخص الصفحة:\n\n" +
                    "• تحتوي هذه الصفحة على حوالي ${words.size} كلمة ممسوحة ضوئياً.\n" +
                    "• من الكلمات المفتاحية الموجودة: ${words.shuffled().take(3).joinToString("، ") { it.text }}.\n\n" +
                    "النص المستخرج:\n$fullText"
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        translationManager.close()
    }
}
