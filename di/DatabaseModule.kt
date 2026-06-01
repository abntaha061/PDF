package com.mohamed.pdfreader.di

import android.content.Context
import androidx.room.Room
import com.mohamed.pdfreader.data.local.BookmarkDao
import com.mohamed.pdfreader.data.local.PdfDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePdfDatabase(@ApplicationContext context: Context): PdfDatabase {
        return Room.databaseBuilder(
            context,
            PdfDatabase::class.java,
            "pdf_reader_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(database: PdfDatabase): BookmarkDao {
        return database.bookmarkDao
    }
}
