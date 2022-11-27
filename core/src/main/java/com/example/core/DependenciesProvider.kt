package com.example.core

import kotlin.reflect.KClass

interface DependenciesProvider {

    fun <T : ProvidableDependency> provide(clazz: KClass<T>): T

    companion object {

        val instance: DependenciesProvider
            get() = TODO("Somehow implemented")
    }
}

inline fun <reified T : ProvidableDependency> DependenciesProvider.provide(): T {
    return provide(T::class)
}