package com.mohamed.pdfreader.di

import android.content.Context
import com.mohamed.pdfreader.data.local.SettingsManager
import com.mohamed.pdfreader.utils.OcrManager
import com.mohamed.pdfreader.utils.TranslationManager
import com.mohamed.pdfreader.utils.TtsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingsManager(@ApplicationContext context: Context): SettingsManager {
        return SettingsManager(context)
    }

    @Provides
    @Singleton
    fun provideTtsManager(
        @ApplicationContext context: Context, 
        settingsManager: SettingsManager
    ): TtsManager {
        return TtsManager(context, settingsManager)
    }

    @Provides
    @Singleton
    fun provideTranslationManager(): TranslationManager {
        return TranslationManager()
    }

    @Provides
    @Singleton
    fun provideOcrManager(): OcrManager {
        return OcrManager()
    }
}
