package com.example.contacts

import dagger.Module

@Module
internal class ModuleWithRequiredParameterModule(
    private val activity: ContactsActivity,
    private val timestamp: Long = System.currentTimeMillis(),
)