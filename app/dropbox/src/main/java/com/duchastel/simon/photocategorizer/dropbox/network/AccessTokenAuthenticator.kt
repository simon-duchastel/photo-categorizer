package com.duchastel.simon.photocategorizer.dropbox.network

import com.duchastel.simon.photocategorizer.auth.AuthProvider
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class OauthAuthenticator(
    private val authProvider: AuthProvider,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request {
        authProvider.executeWithAuthToken(
            execute = { token ->

            },
            onError = {

            }
        )

        return response.request
//        { accessToken, _, ex ->
//            if (ex != null) {
//                Log.e("AppAuthAuthenticator", "Failed to authorize = $ex")
//            }
//
//            if (response.request().header("Authorization") != null) {
//                future.complete(null) // Give up, we've already failed to authenticate.
//            }
//
//            val response = response.request().newBuilder()
//                .header("Authorization", "Bearer $accessToken")
//                .build()
//
//            future.complete(response)
//        }
    }

}