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
            .requiredParameterModule(RequiredParameterModule(this))
            .allDefaultParametersModule(AllDefaultParametersModule()) // we can omit it
            .unknownDependencies(object : UnknownDependencies {})
            .networkDependencies(DependenciesProvider.instance.provide()) // we can omit it
            .internalProvidableDependencies(DependenciesProvider.instance.provide()) // we can omit it
            .build()
            .inject(this)

        // VS

        ContactsComponentFactory.createComponent(
            requiredParameterModule = RequiredParameterModule(this),
            unknownDependencies = object : UnknownDependencies {},
        ).inject(this)
    }
}