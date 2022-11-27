package com.example.contacts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.core.DependenciesProvider
import com.example.core.provide
import okhttp3.OkHttpClient
import javax.inject.Inject

internal class ContactsActivity : AppCompatActivity() {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerContactsComponent.builder()
            .contactsModule(ContactsModule(this))
            .networkDependencies(DependenciesProvider.instance.provide())
            .build()
            .inject(this)
    }
}