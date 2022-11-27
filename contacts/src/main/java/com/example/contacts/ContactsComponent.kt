package com.example.contacts

import com.example.network.NetworkDependencies
import dagger.Component

@Component(
    modules = [
        ContactsModule::class,
    ],
    dependencies = [
        NetworkDependencies::class,
    ]
)
internal interface ContactsComponent {

    fun inject(activity: ContactsActivity)
}