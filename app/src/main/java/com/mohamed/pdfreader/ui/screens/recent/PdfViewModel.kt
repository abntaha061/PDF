package com.mohamed.pdfreader.ui.screens.recent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohamed.pdfreader.domain.model.PdfFile
import com.mohamed.pdfreader.utils.StorageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _pdfFiles = MutableStateFlow<List<PdfFile>>(emptyList())
    val pdfFiles: StateFlow<List<PdfFile>> = _pdfFiles

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadPdfs() {
        viewModelScope.launch {
            _isLoading.value = true
            val files = StorageUtils.getAllPdfs(getApplication())
            _pdfFiles.value = files
            _isLoading.value = false
        }
    }
}
