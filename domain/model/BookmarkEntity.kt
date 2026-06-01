package com.mohamed.pdfreader.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileUri: String,
    val pageIndex: Int,
    val title: String,
    val timestamp: Long
)
