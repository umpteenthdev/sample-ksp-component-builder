package com.example.contacts

import dagger.Module

@Module
internal class RequiredParameterModule(
    private val activity: ContactsActivity,
)