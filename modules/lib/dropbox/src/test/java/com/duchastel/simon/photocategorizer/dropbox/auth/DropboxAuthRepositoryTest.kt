package com.duchastel.simon.photocategorizer.dropbox.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class DropboxAuthRepositoryTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    private lateinit var repository: DropboxAuthRepository

    @Before
    fun setUp() {
        context = mock<Context>()
        sharedPreferences = mock<SharedPreferences>()
        sharedPreferencesEditor = mock<SharedPreferences.Editor>()

        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.putString(any(), any())).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.commit()).thenReturn(true)
        whenever(sharedPreferences.getString(any(), any())).thenReturn(null)

        repository = DropboxAuthRepository(context)
    }

    @Test
    fun `isLoggedIn should return false when no auth state exists`() {
        assertFalse(repository.isLoggedIn())
    }

    @Test
    fun `isLoggedInFlow should emit false when no auth state exists`() = runBlocking {
        val result = repository.isLoggedInFlow().first()
        assertFalse(result)
    }

    @Test
    fun `refreshToken should return false when not logged in`() = runBlocking {
        val result = repository.refreshToken()
        assertFalse(result)
    }

    @Test
    fun `getAccessTokenOrRefresh should return null when not logged in`() = runBlocking {
        val result = repository.getAccessTokenOrRefresh()
        assertNull(result)
    }

    @Test
    fun `logout should update state to logged out`() {
        repository.logout()
        assertFalse(repository.isLoggedIn())
        verify(sharedPreferencesEditor).putString(any(), any())
        verify(sharedPreferencesEditor).commit()
    }

    @Test
    fun `executeWithAuthToken should throw exception when not logged in`() = runBlocking {
        try {
            repository.executeWithAuthToken { token ->
                "Should not reach here"
            }
            fail("Expected exception to be thrown")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("USER NOT SIGNED IN") == true)
        }
    }
}