package com.example.contacts

import com.example.core.GenerateComponentFactory
import com.example.network.NetworkDependencies
import dagger.Component

@GenerateComponentFactory
@Component(
    modules = [
        ModuleWithRequiredParameterModule::class,
        InterfaceModule::class,
    ],
    dependencies = [
        NetworkDependencies::class,
        InternalProvidableDependencies::class,
        UnknownDependencies::class,
    ]
)
internal interface ContactsComponent {

    fun inject(activity: ContactsActivity)
}