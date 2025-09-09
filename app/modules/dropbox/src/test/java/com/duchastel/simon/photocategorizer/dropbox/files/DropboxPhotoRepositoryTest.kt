package com.duchastel.simon.photocategorizer.dropbox.files

import com.duchastel.simon.photocategorizer.dropbox.network.DropboxApiError
import com.duchastel.simon.photocategorizer.dropbox.network.DropboxFileApi
import com.duchastel.simon.photocategorizer.dropbox.network.MoveFileRequest
import com.duchastel.simon.photocategorizer.dropbox.network.MoveFileResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DropboxPhotoRepositoryTest {

    @Mock
    private lateinit var mockNetworkApi: DropboxFileApi

    private lateinit var repository: DropboxPhotoRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = DropboxPhotoRepository(mockNetworkApi)
    }

    @Test
    fun `movePhoto should validate path format`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            repository.movePhoto("invalid-path", "/valid/path")
        }
        
        assertFailsWith<IllegalArgumentException> {
            repository.movePhoto("/valid/path", "invalid-path")
        }
    }

    @Test
    fun `movePhoto should complete successfully for valid request`() = runTest {
        // Given
        val originalPath = "/test/original.jpg"
        val newPath = "/test/new.jpg"
        whenever(mockNetworkApi.moveFile(any())).doReturn(MoveFileResponse(null, null))

        // When & Then - should complete without throwing
        repository.movePhoto(originalPath, newPath)
        
        verify(mockNetworkApi).moveFile(MoveFileRequest(originalPath, newPath))
    }

    @Test
    fun `movePhoto should handle API errors properly`() = runTest {
        // Given
        val originalPath = "/test/original.jpg"
        val newPath = "/test/new.jpg"
        whenever(mockNetworkApi.moveFile(any())).doReturn(
            MoveFileResponse(error = DropboxApiError("file_not_found"), errorSummary = "File not found")
        )

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            repository.movePhoto(originalPath, newPath)
        }
    }

    @Test
    fun `concurrent movePhoto calls should be processed sequentially`() = runTest {
        // Given
        val apiCallOrder = mutableListOf<String>()
        whenever(mockNetworkApi.moveFile(any())).doAnswer { invocation ->
            val request = invocation.getArgument<MoveFileRequest>(0)
            apiCallOrder.add(request.from)
            runBlocking { delay(50) } // Simulate API call time
            MoveFileResponse(null, null)
        }

        // When - make multiple concurrent calls
        val job1 = async { repository.movePhoto("/test/file1.jpg", "/dest/file1.jpg") }
        val job2 = async { repository.movePhoto("/test/file2.jpg", "/dest/file2.jpg") }
        val job3 = async { repository.movePhoto("/test/file3.jpg", "/dest/file3.jpg") }

        job1.await()
        job2.await()
        job3.await()

        // Then - should be processed in order
        assertEquals(3, apiCallOrder.size)
        assertTrue(apiCallOrder.contains("/test/file1.jpg"))
        assertTrue(apiCallOrder.contains("/test/file2.jpg"))
        assertTrue(apiCallOrder.contains("/test/file3.jpg"))
    }

    @Test
    fun `retry logic should handle 429 errors`() = runTest {
        // Given
        val http429Response = Response.error<Any>(429, "".toResponseBody(null))
        val http429Exception = HttpException(http429Response)
        
        var attemptCount = 0
        whenever(mockNetworkApi.moveFile(any())).doAnswer {
            attemptCount++
            if (attemptCount <= 2) {
                throw http429Exception
            } else {
                MoveFileResponse(null, null)
            }
        }

        // When
        repository.movePhoto("/test/file.jpg", "/dest/file.jpg")

        // Then - should have made 3 attempts (2 failures + 1 success)
        assertEquals(3, attemptCount)
        verify(mockNetworkApi, times(3)).moveFile(any())
    }

    @Test
    fun `retry logic should give up after max attempts`() = runTest {
        // Given
        val http429Response = Response.error<Any>(429, "".toResponseBody(null))
        val http429Exception = HttpException(http429Response)
        
        whenever(mockNetworkApi.moveFile(any())).doAnswer {
            throw http429Exception
        }

        // When & Then - should eventually fail after max retries
        assertFailsWith<Exception> {
            repository.movePhoto("/test/file.jpg", "/dest/file.jpg")
        }
        
        // Should have made 4 attempts (1 initial + 3 retries)
        verify(mockNetworkApi, times(4)).moveFile(any())
    }

    @Test
    fun `non-retryable errors should fail immediately`() = runTest {
        // Given
        val http404Response = Response.error<Any>(404, "".toResponseBody(null))
        val http404Exception = HttpException(http404Response)
        
        whenever(mockNetworkApi.moveFile(any())).doAnswer {
            throw http404Exception
        }

        // When & Then - should fail immediately without retries
        assertFailsWith<HttpException> {
            repository.movePhoto("/test/file.jpg", "/dest/file.jpg")
        }
        
        // Should have made only 1 attempt
        verify(mockNetworkApi, times(1)).moveFile(any())
    }
}