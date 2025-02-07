package com.duchastel.simon.photocategorizer.auth

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class LoggedOutInterceptor(
    private val authManager: AuthManager,
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request()).also { response ->
            if (response.code == 401) {
                // TODO - handle this error better
                authManager.logout() // logout defensively
            }
        }
    }
}