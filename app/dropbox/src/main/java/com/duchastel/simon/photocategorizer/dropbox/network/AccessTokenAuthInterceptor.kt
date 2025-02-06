package com.duchastel.simon.photocategorizer.dropbox.network

import com.duchastel.simon.photocategorizer.auth.AuthProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AccessTokenAuthInterceptor(
    private val authProvider: AuthProvider,
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authedRequest = try {
            runBlocking {
                authProvider.executeWithAuthToken { token ->
                    request.newBuilder()
                        .header("Authorization", "Bearer ${token.accessToken}")
                        .build()
                }
            }
        } catch (ex: Exception) {
            // TODO - handle error
            request
        }

        return chain.proceed(authedRequest)
    }
}