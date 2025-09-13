package com.duchastel.simon.photocategorizer.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class AuthRetryInterceptor(
    private val authRepository: AuthRepository,
) : Interceptor {
    
    private val refreshLock = ReentrantLock()
    private var isRefreshing = false
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalResponse = chain.proceed(originalRequest)
        
        // If not a 401, return the response as-is
        if (originalResponse.code != 401) {
            return originalResponse
        }
        
        // Handle 401: attempt token refresh and retry
        originalResponse.close() // Close original response to free resources
        
        return try {
            val refreshSucceeded = refreshLock.withLock {
                if (!isRefreshing) {
                    isRefreshing = true
                    try {
                        runBlocking { authRepository.refreshToken() }
                    } finally {
                        isRefreshing = false
                    }
                } else {
                    // Another thread is already refreshing, wait and check result
                    true // Assume success, will be validated by retry attempt
                }
            }
            
            if (refreshSucceeded) {
                // Retry the original request with the new token
                val newToken = runBlocking { authRepository.getAccessTokenOrRefresh() }
                if (newToken != null) {
                    val retryRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer ${newToken.accessToken}")
                        .build()
                    chain.proceed(retryRequest)
                } else {
                    // Token refresh indicated success but we can't get a token - pass through
                    // the 401 to let LoggedOutInterceptor handle logout
                    chain.proceed(originalRequest)
                }
            } else {
                // Refresh failed - pass through the 401 to let LoggedOutInterceptor handle logout
                chain.proceed(originalRequest)
            }
        } catch (e: Exception) {
            // Any error during refresh - pass through the 401 to let LoggedOutInterceptor handle logout
            chain.proceed(originalRequest)
        }
    }
}