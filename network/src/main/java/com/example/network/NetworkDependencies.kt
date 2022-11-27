package com.example.network

import okhttp3.OkHttpClient

interface NetworkDependencies {

    fun geOkHttpClient(): OkHttpClient
}