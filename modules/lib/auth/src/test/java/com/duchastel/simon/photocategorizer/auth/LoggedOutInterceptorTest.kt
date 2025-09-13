package com.duchastel.simon.photocategorizer.auth

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class LoggedOutInterceptorTest {

    private val authRepository = mock<AuthRepository>()
    private val interceptor = LoggedOutInterceptor(authRepository)
    private val chain = mock<Interceptor.Chain>()

    @Test
    fun `should pass through non-401 responses unchanged`() {
        val request = Request.Builder()
            .url("https://example.com")
            .build()
        
        val originalResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(ResponseBody.create(null, "success"))
            .build()

        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(request)).thenReturn(originalResponse)

        val result = interceptor.intercept(chain)

        assertEquals(200, result.code)
        assertEquals("OK", result.message)
        assertEquals("success", result.body?.string())
    }

    @Test
    fun `should transform 401 response to 200 and call logout`() {
        val request = Request.Builder()
            .url("https://example.com")
            .build()
        
        val unauthorizedResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body(ResponseBody.create(null, "unauthorized"))
            .build()

        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(request)).thenReturn(unauthorizedResponse)

        val result = interceptor.intercept(chain)

        verify(authRepository).logout()
        assertEquals(200, result.code)
        assertEquals("Logged out", result.message)
        assertEquals("", result.body?.string())
    }

    @Test
    fun `should pass through non-401 error codes unchanged`() {
        val request = Request.Builder()
            .url("https://example.com")
            .build()
        
        val errorResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body(ResponseBody.create(null, "server error"))
            .build()

        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(request)).thenReturn(errorResponse)

        val result = interceptor.intercept(chain)

        assertEquals(500, result.code)
        assertEquals("Internal Server Error", result.message)
        assertEquals("server error", result.body?.string())
    }
}