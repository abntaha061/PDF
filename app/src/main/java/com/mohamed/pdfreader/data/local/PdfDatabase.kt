package com.mohamed.pdfreader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mohamed.pdfreader.domain.model.BookmarkEntity

@Database(entities = [BookmarkEntity::class], version = 1, exportSchema = false)
abstract class PdfDatabase : RoomDatabase() {
    abstract val bookmarkDao: BookmarkDao
}
