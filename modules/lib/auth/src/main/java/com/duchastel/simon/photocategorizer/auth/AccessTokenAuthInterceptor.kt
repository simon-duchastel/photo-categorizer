package com.duchastel.simon.photocategorizer.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AccessTokenAuthInterceptor(
    private val authRepository: AuthRepository,
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authedRequest = try {
            runBlocking {
                val token = authRepository.getAccessTokenOrRefresh()
                if (token != null) {
                    request.newBuilder()
                        .header("Authorization", "Bearer ${token.accessToken}")
                        .build()
                } else {
                    // No valid token available - proceed with original request
                    // This will likely result in 401, which AuthRetryInterceptor will handle
                    request
                }
            }
        } catch (ex: Exception) {
            // If token retrieval fails, proceed with original request
            // This will likely result in 401, which AuthRetryInterceptor will handle
            request
        }

        return chain.proceed(authedRequest)
    }
}