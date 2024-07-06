package com.example.githubrepoviewer.di

import android.content.Context
import com.example.githubrepoviewer.data.KeyValueStorage
import com.example.githubrepoviewer.ui.providers.ResourcesProvider
import com.example.githubrepoviewer.ui.providers.ResourcesProviderImpl
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
    fun provideKeyValueStorage(@ApplicationContext context: Context): KeyValueStorage {
        return KeyValueStorage(context)
    }
    @Provides
    @Singleton
    fun provideStringProvider(@ApplicationContext context: Context): ResourcesProvider {
        return ResourcesProviderImpl(context)
    }
}
