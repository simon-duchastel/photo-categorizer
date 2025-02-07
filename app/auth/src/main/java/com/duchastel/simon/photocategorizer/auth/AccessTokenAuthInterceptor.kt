package com.duchastel.simon.photocategorizer.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AccessTokenAuthInterceptor(
    private val authManager: AuthManager,
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authedRequest = try {
            runBlocking {
                authManager.executeWithAuthToken { token ->
                    request.newBuilder()
                        .header("Authorization", "Bearer ${token.accessToken}")
                        .build()
                }
            }
        } catch (ex: Exception) {
            // TODO - handle error better
            request
        }

        return chain.proceed(authedRequest)
    }
}