package com.mohamed.pdfreader.ui.screens.search

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
class SearchViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<PdfFile>>(emptyList())
    val searchResults: StateFlow<List<PdfFile>> = _searchResults

    private var allFiles: List<PdfFile> = emptyList()

    init {
        viewModelScope.launch {
            allFiles = StorageUtils.getAllPdfs(getApplication())
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            _searchResults.value = allFiles.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
}
