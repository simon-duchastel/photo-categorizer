package com.duchastel.simon.photocategorizer.dropbox.network

import com.duchastel.simon.photocategorizer.auth.AuthProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class OauthAuthenticator(
    private val authProvider: AuthProvider,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        return try {
            runBlocking {
                authProvider.executeWithAuthToken { token ->
                    if (response.request.header("Authorization") != null) {
                        null // We've already tried adding an authorization header and failed.
                    } else {
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${token.accessToken}")
                            .build()
                    }
                }
            }
        } catch (ex: Exception) {
            // TODO - handle error
            null
        }
    }
}
