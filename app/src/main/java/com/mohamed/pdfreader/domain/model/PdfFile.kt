package com.mohamed.pdfreader.domain.model

import android.net.Uri

data class PdfFile(
    val id: Long,
    val name: String,
    val uri: Uri,
    val size: Long,
    val dateModified: Long,
    val path: String
)
