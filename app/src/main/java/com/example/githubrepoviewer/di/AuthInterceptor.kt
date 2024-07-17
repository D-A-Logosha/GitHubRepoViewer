package com.example.githubrepoviewer.di

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        this.authToken = token
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val requestBuilder = originalRequest.newBuilder()
            .method(originalRequest.method, originalRequest.body)

        authToken?.let {
            requestBuilder.header("Authorization", it)
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
