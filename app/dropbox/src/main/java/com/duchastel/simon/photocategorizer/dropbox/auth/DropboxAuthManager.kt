package com.duchastel.simon.photocategorizer.dropbox.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.core.net.toUri
import com.duchastel.simon.photocategorizer.auth.AuthManager
import com.duchastel.simon.photocategorizer.auth.AuthToken
import com.duchastel.simon.photocategorizer.dropbox.network.DROPBOX_CLIENT_ID
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationException.TYPE_GENERAL_ERROR
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenResponse
import org.json.JSONException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
internal class DropboxAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
): AuthManager {

    // State and config

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "DropboxAuthProvider",
        Context.MODE_PRIVATE,
    )

    private val config: AuthorizationServiceConfiguration =
        AuthorizationServiceConfiguration(AUTH_ENDPOINT, TOKEN_ENDPOINT)
    private val authService = AuthorizationService(context)

    private val loggedOutAuthState = State(AuthState(config))
    private val state = MutableStateFlow(readAuthState())

    // Public functions

    override fun isLoggedIn(): Boolean {
        return state.value.isLoggedIn
    }

    override fun isLoggedInFlow(): Flow<Boolean> {
        return state.map { it.isLoggedIn }.distinctUntilChanged()
    }

    override suspend fun login(redirectIntent: PendingIntent): Boolean {
        state.update { oldState -> oldState.copy(loginResult = null) }
        try {
            val loginRequest = AuthorizationRequest.Builder(
                /* configuration = */ config,
                /* clientId = */ DROPBOX_CLIENT_ID,
                /* responseType = */ "code",
                /* redirectUri = */ REDIRECT_URI,
            ).build()

            authService.performAuthorizationRequest(
                loginRequest,
                redirectIntent,
            )

            // wait for the loginResult to complete
            return state.mapNotNull { it.loginResult != null }.first()
        } finally {
            // for consistency, null out the state if an exception is thrown
            state.update { oldState -> oldState.copy(loginResult = null) }
        }
    }

    override fun logout() {
        updateAuthStateToLoggedOut()
    }

    override fun processIntent(intent: Intent) {
        val authResponse = AuthorizationResponse.fromIntent(intent)
        val authError = AuthorizationException.fromIntent(intent)

        // if both are null, this intent isn't an auth intent
        if (authResponse == null && authError == null) return

        updateAuthState(authResponse, authError)
        if (authResponse != null) {
            authService.performTokenRequest(
                authResponse.createTokenExchangeRequest(),
            ) { response, error ->
                updateAuthState(response, error)
            }
        }
    }

    override suspend fun <T> executeWithAuthToken(
        execute: suspend (authToken: AuthToken) -> T,
    ): T {
        val authToken: AuthToken = suspendCoroutine { continuation ->
            state.value.authState.accessToken.let {
                if (it != null) {
                    continuation.resume(AuthToken(it))
                } else {
                    continuation.resumeWithException(USER_NOT_SIGNED_IN_EXCEPTION)
                }
            }
        }

        return execute(authToken)
    }

    // Private functions

    private fun writeAuthState() {
        sharedPreferences.edit(commit = true) {
            putString(PREFS_AUTH_STATE, state.value.toJsonString())
        }
    }

    private fun readAuthState(): State {
        val jsonString = sharedPreferences.getString(PREFS_AUTH_STATE, null)
        return jsonString?.let { State.fromJsonString(it) } ?: loggedOutAuthState
    }

    private fun updateAuthStateToLoggedOut() {
        synchronized(state) {
            state.update { loggedOutAuthState }
            writeAuthState()
        }
    }

    private fun updateAuthState(
       response: AuthorizationResponse?,
       error: AuthorizationException?,
    ) {
        synchronized(state) {
            state.update { oldState ->
                val authState = oldState.authState.apply {
                    update(response, error)
                }
                oldState.copy(
                    authState = authState,
                    isLoggedIn = authState.isAuthorized,

                    // error means the login failed, but authorization success
                    // doesn't mean the full login succeeded yet
                    loginResult = if (error == null) false else null,
                )
            }
            writeAuthState()
        }
    }

    private fun updateAuthState(
        response: TokenResponse?,
        error: AuthorizationException?,
    ) {
        synchronized(state) {
            state.update { oldState ->
                val authState = oldState.authState.apply {
                    update(response, error)
                }
                oldState.copy(
                    authState = authState,
                    isLoggedIn = authState.isAuthorized,

                    // token response is the last step for login, so a success
                    // here means the entire login succeeded
                    loginResult = response != null && error == null,
                )
            }
            writeAuthState()
        }
    }

    /**
     * Wrapper class to prevent calls to [AuthState.update].
     * [AuthManager] must be able to observe all changes to AuthState,
     * so immutable data must be used
     */
    private data class State(
        val authState: AuthState,
        val isLoggedIn: Boolean = authState.isAuthorized,

        // null if no login in progress, true if login succeeded, false otherwise
        val loginResult: Boolean? = null,
    ) {
        fun toJsonString(): String {
            return authState.jsonSerializeString()
        }

        companion object {
            fun fromJsonString(jsonString: String): State? {
                return try {
                    AuthState.jsonDeserialize(jsonString)?.let { State(it) }
                } catch (ex: IllegalArgumentException) {
                    null
                } catch (ex: JSONException) {
                    null
                }
            }
        }
    }

    // Constants

    companion object {
        private val AUTH_ENDPOINT = "https://www.dropbox.com/oauth2/authorize".toUri()
        private val TOKEN_ENDPOINT = "https://www.dropbox.com/oauth2/token".toUri()
        private val REDIRECT_URI = "https://duchastel.com".toUri()

        private const val PREFS_AUTH_STATE = "AUTH_STATE"

        private val USER_NOT_SIGNED_IN_EXCEPTION = AuthorizationException(
            /* type = */ TYPE_GENERAL_ERROR,
            /* code = */ 99,
            /* error = */ "USER NOT SIGNED IN",
            /* errorDescription = */ "No auth tokens found. Have you called login()?",
            /* errorUri = */ null,
            /* rootCause = */ null
        )
    }
}
