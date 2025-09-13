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

class AccessTokenAuthInterceptorTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var interceptor: AccessTokenAuthInterceptor
    private lateinit var chain: Interceptor.Chain

    @Before
    fun setUp() {
        authRepository = mock<AuthRepository>()
        interceptor = AccessTokenAuthInterceptor(authRepository)
        chain = mock<Interceptor.Chain>()
    }

    @Test
    fun `should add Authorization header when token is available`() {
        val request = createMockRequest()
        val response = createMockResponse(200, "OK")
        val token = AuthToken("test-access-token")

        whenever(chain.request()).thenReturn(request)
        whenever(runBlocking { authRepository.getAccessTokenOrRefresh() }).thenReturn(token)
        
        val expectedRequest = request.newBuilder()
            .header("Authorization", "Bearer test-access-token")
            .build()
        whenever(chain.proceed(any())).thenReturn(response)

        val result = interceptor.intercept(chain)

        assertEquals(200, result.code)
        verify(chain).proceed(argThat { req ->
            req.header("Authorization") == "Bearer test-access-token"
        })
    }

    @Test
    fun `should proceed with original request when token is not available`() {
        val request = createMockRequest()
        val response = createMockResponse(401, "Unauthorized")

        whenever(chain.request()).thenReturn(request)
        whenever(runBlocking { authRepository.getAccessTokenOrRefresh() }).thenReturn(null)
        whenever(chain.proceed(request)).thenReturn(response)

        val result = interceptor.intercept(chain)

        assertEquals(401, result.code)
        verify(chain).proceed(request) // Original request without auth header
    }

    @Test
    fun `should proceed with original request when token retrieval throws exception`() {
        val request = createMockRequest()
        val response = createMockResponse(401, "Unauthorized")

        whenever(chain.request()).thenReturn(request)
        whenever(runBlocking { authRepository.getAccessTokenOrRefresh() }).thenThrow(RuntimeException("Token retrieval failed"))
        whenever(chain.proceed(request)).thenReturn(response)

        val result = interceptor.intercept(chain)

        assertEquals(401, result.code)
        verify(chain).proceed(request) // Original request without auth header
    }

    @Test
    fun `should handle empty token gracefully`() {
        val request = createMockRequest()
        val response = createMockResponse(401, "Unauthorized")
        val emptyToken = AuthToken("")

        whenever(chain.request()).thenReturn(request)
        whenever(runBlocking { authRepository.getAccessTokenOrRefresh() }).thenReturn(emptyToken)
        whenever(chain.proceed(any())).thenReturn(response)

        val result = interceptor.intercept(chain)

        assertEquals(401, result.code)
        verify(chain).proceed(argThat { req ->
            req.header("Authorization") == "Bearer "
        })
    }

    @Test
    fun `should preserve existing headers when adding Authorization header`() {
        val request = Request.Builder()
            .url("https://api.dropboxapi.com/2/files/list_folder")
            .header("Content-Type", "application/json")
            .header("User-Agent", "PhotoCategorizer/1.0")
            .build()
        
        val response = createMockResponse(200, "OK")
        val token = AuthToken("test-access-token")

        whenever(chain.request()).thenReturn(request)
        whenever(runBlocking { authRepository.getAccessTokenOrRefresh() }).thenReturn(token)
        whenever(chain.proceed(any())).thenReturn(response)

        val result = interceptor.intercept(chain)

        assertEquals(200, result.code)
        verify(chain).proceed(argThat { req ->
            req.header("Content-Type") == "application/json" &&
            req.header("User-Agent") == "PhotoCategorizer/1.0" &&
            req.header("Authorization") == "Bearer test-access-token"
        })
    }

    @Test
    fun `should replace existing Authorization header when token is available`() {
        val request = Request.Builder()
            .url("https://api.dropboxapi.com/2/files/list_folder")
            .header("Authorization", "Bearer old-token")
            .build()
        
        val response = createMockResponse(200, "OK")
        val token = AuthToken("new-access-token")

        whenever(chain.request()).thenReturn(request)
        whenever(runBlocking { authRepository.getAccessTokenOrRefresh() }).thenReturn(token)
        whenever(chain.proceed(any())).thenReturn(response)

        val result = interceptor.intercept(chain)

        assertEquals(200, result.code)
        verify(chain).proceed(argThat { req ->
            req.header("Authorization") == "Bearer new-access-token"
        })
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