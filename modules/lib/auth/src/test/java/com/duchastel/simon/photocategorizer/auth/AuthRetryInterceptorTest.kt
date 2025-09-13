package com.duchastel.simon.photocategorizer.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.io.IOException

class AuthRetryInterceptorTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var interceptor: AuthRetryInterceptor
    private lateinit var chain: Interceptor.Chain

    @Before
    fun setUp() {
        authRepository = mock<AuthRepository>()
        interceptor = AuthRetryInterceptor(authRepository)
        chain = mock<Interceptor.Chain>()
    }

    @Test
    fun `non-401 response should pass through unchanged`() {
        val request = createMockRequest()
        val successResponse = createMockResponse(200, "OK")

        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(request)).thenReturn(successResponse)

        val result = interceptor.intercept(chain)

        assertEquals(200, result.code)
        // Should not call any auth repository methods for non-401 responses
    }

    @Test
    fun `401 response with successful token refresh should retry request`() {
        val request = createMockRequest()
        val unauthorizedResponse = createMockResponse(401, "Unauthorized")
        val successResponse = createMockResponse(200, "OK")
        val newToken = AuthToken("new-access-token")

        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(request)).thenReturn(unauthorizedResponse)
        
        whenever(runBlocking { authRepository.refreshToken() }).thenReturn(true)
        whenever(runBlocking { authRepository.getAccessTokenOrRefresh() }).thenReturn(newToken)
        
        val expectedRetryRequest = request.newBuilder()
            .header("Authorization", "Bearer new-access-token")
            .build()
        whenever(chain.proceed(argThat { req: Request -> req.header("Authorization") == "Bearer new-access-token" }))
            .thenReturn(successResponse)

        val result = interceptor.intercept(chain)

        assertEquals(200, result.code)
        verify(chain).proceed(argThat { req: Request -> req.header("Authorization") == "Bearer new-access-token" })
    }

    @Test
    fun `401 response with failed token refresh should pass through original request`() {
        val request = createMockRequest()
        val unauthorizedResponse = createMockResponse(401, "Unauthorized")
        val finalUnauthorizedResponse = createMockResponse(401, "Unauthorized")

        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(request)).thenReturn(unauthorizedResponse, finalUnauthorizedResponse)
        
        whenever(runBlocking { authRepository.refreshToken() }).thenReturn(false)

        val result = interceptor.intercept(chain)

        assertEquals(401, result.code)
        verify(chain, times(2)).proceed(request) // Original + retry with same request
    }

    @Test
    fun `401 response with successful refresh but no token should pass through original request`() {
        val request = createMockRequest()
        val unauthorizedResponse = createMockResponse(401, "Unauthorized")
        val finalUnauthorizedResponse = createMockResponse(401, "Unauthorized")

        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(request)).thenReturn(unauthorizedResponse, finalUnauthorizedResponse)
        
        whenever(runBlocking { authRepository.refreshToken() }).thenReturn(true)
        whenever(runBlocking { authRepository.getAccessTokenOrRefresh() }).thenReturn(null)

        val result = interceptor.intercept(chain)

        assertEquals(401, result.code)
        verify(chain, times(2)).proceed(request) // Original + retry with same request
    }

    @Test
    fun `401 response with refresh exception should pass through original request`() {
        val request = createMockRequest()
        val unauthorizedResponse = createMockResponse(401, "Unauthorized")
        val finalUnauthorizedResponse = createMockResponse(401, "Unauthorized")

        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(request)).thenReturn(unauthorizedResponse, finalUnauthorizedResponse)
        
        whenever(runBlocking { authRepository.refreshToken() }).thenThrow(RuntimeException("Refresh failed"))

        val result = interceptor.intercept(chain)

        assertEquals(401, result.code)
        verify(chain, times(2)).proceed(request) // Original + retry with same request
    }

    private fun createMockRequest(): Request {
        return Request.Builder()
            .url("https://api.dropboxapi.com/2/files/list_folder")
            .build()
    }

    private fun createMockResponse(code: Int, message: String): Response {
        return Response.Builder()
            .request(createMockRequest())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body("".toResponseBody("application/json".toMediaType()))
            .build()
    }
}