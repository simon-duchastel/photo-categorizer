package com.duchastel.simon.photocategorizer.dropbox.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.duchastel.simon.photocategorizer.auth.AuthProvider
import com.duchastel.simon.photocategorizer.auth.AuthToken
import dagger.hilt.android.qualifiers.ApplicationContext
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationException.TYPE_GENERAL_ERROR
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
internal class DropboxAuthProvider @Inject constructor(
    @ApplicationContext private val context: Context,
): AuthProvider {
    private val config: AuthorizationServiceConfiguration =
        AuthorizationServiceConfiguration(AUTH_ENDPOINT, TOKEN_ENDPOINT)
    private val authService = AuthorizationService(context);

    private val authState = AuthState(config)

    override fun login(redirectIntent: PendingIntent) {
        val loginRequest = AuthorizationRequest.Builder(
            /* configuration = */ config,
            /* clientId = */ CLIENT_ID,
            /* responseType = */ "code",
            /* redirectUri = */ REDIRECT_URI,
        ).build()

        authService.performAuthorizationRequest(
            loginRequest,
            redirectIntent,
        )
    }

    override fun processIntent(intent: Intent) {
        val authResponse = AuthorizationResponse.fromIntent(intent)
        val authError = AuthorizationException.fromIntent(intent)

        // if both are null, this intent isn't an auth intent
        if (authResponse == null && authError == null) return

        authState.update(authResponse, authError)
        if (authResponse != null) {
            authService.performTokenRequest(
                authResponse.createTokenExchangeRequest(),
            ) { resp, ex ->
                authState.update(resp, ex)
            }
        }
    }

    override suspend fun <T> executeWithAuthToken(
        execute: suspend (authToken: AuthToken) -> T,
    ): T {
        val accessToken: AuthToken = suspendCoroutine { continuation ->
            authState.apply {
                performActionWithFreshTokens(authService) { accessToken, _, error ->
                    if (error != null || accessToken == null) {
                        val exception = error ?: AuthorizationException(
                            /* type = */ TYPE_GENERAL_ERROR,
                            /* code = */ 99,
                            /* error = */ "USER NOT SIGNED IN",
                            /* errorDescription = */ "No auth tokens found. Have you called login()?",
                            /* errorUri = */ null,
                            /* rootCause = */ null
                        )
                        continuation.resumeWithException(exception)
                        return@performActionWithFreshTokens
                    }

                    try {
                        continuation.resume(AuthToken(accessToken))
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            }
        }

        return execute(accessToken)
    }

    companion object {
        private const val CLIENT_ID = "qlq2l578dxtpcum"
        private val AUTH_ENDPOINT = "https://www.dropbox.com/oauth2/authorize".toUri()
        private val TOKEN_ENDPOINT = "https://www.dropbox.com/oauth2/token".toUri()
        private val REDIRECT_URI = "https://duchastel.com".toUri()
    }
}
