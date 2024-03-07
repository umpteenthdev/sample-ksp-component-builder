package com.example.contacts

import dagger.Module
import dagger.Provides
import java.util.UUID

@Module
internal class AllDefaultParametersModule(
    private val key: String = UUID.randomUUID().toString(),
    private val timestamp: Long = System.currentTimeMillis(),
) {

    @Provides
    fun provideUniqueId(): String {
        return key
    }
}