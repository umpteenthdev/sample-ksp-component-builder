package com.example.contacts

import dagger.Module
import dagger.Provides
import java.io.File

@Module
internal class RequiredParameterModule(
    private val activity: ContactsActivity,
) {

    @Provides
    fun provideCacheDir(): File {
        return activity.cacheDir
    }
}