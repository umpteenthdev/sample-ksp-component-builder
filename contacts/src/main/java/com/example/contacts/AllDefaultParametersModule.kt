package com.example.contacts

import dagger.Module
import java.util.UUID

@Module
internal class AllDefaultParametersModule(
    private val key: String = UUID.randomUUID().toString(),
    private val timestamp: Long = System.currentTimeMillis(),
)