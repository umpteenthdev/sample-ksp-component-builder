package com.example.contacts

import androidx.activity.OnBackPressedDispatcher
import dagger.Module
import dagger.Provides

@Module
internal class ContactsModule(
    private val activity: ContactsActivity,
) {

    @Provides
    fun provideOnBackPressedDispatcher(): OnBackPressedDispatcher {
        return activity.onBackPressedDispatcher
    }
}