package com.example.network

import com.example.core.ProvidableDependency
import okhttp3.OkHttpClient

interface NetworkDependencies : ProvidableDependency {

    fun geOkHttpClient(): OkHttpClient
}