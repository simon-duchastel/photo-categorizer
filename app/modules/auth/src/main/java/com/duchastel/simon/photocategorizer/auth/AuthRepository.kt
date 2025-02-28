package com.duchastel.simon.photocategorizer.auth

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /**
     * Returns true if the user is currently logged in, false otherwise.
     */
    fun isLoggedIn(): Boolean

    /**
     * Same as [isLoggedIn], but as a flow of logged in events.
     */
    fun isLoggedInFlow(): Flow<Boolean>

    /**
     * Begins the login flow. Note that this may launch a Chrome Custom Tab to login the user,
     * so expect this to potentially cause the user to leave your activity.
     *
     * Returns true if the user successfully logged in, false otherwise.
     */
    suspend fun login(redirectIntent: PendingIntent): Boolean

    /**
     * Logout the current user.
     */
    fun logout()

    /**
     * Must call onNewIntent in your [Activity.onNewIntent] and [Activity.onCreate] methods.
     */
    fun processIntent(intent: Intent)

    /**
     * Execute an API request with a valid AuthToken. May refresh the token under the hood,
     * so expect this call to potentially be long-running.
     *
     * Must be called after successfully logging in.
     */
    suspend fun <T> executeWithAuthToken(
        execute: suspend (authToken: AuthToken) -> T,
    ): T
}

data class AuthToken(
    val accessToken: String
)
