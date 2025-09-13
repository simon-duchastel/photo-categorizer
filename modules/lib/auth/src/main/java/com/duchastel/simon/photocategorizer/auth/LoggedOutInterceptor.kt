package com.duchastel.simon.photocategorizer.auth

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException

class LoggedOutInterceptor(
    private val authRepository: AuthRepository,
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain
            .proceed(chain.request())
            .let { response ->
                // TODO - handle this behavior better
                if (response.code == 401) {
                    authRepository.logout() // logout defensively
                    response.newBuilder()
                        .code(200) // Transform to success to avoid crashes
                        .message("Logged out")
                        .body(ResponseBody.create(null, ""))
                        .build()
                } else {
                    response
                }
            }
    }
}