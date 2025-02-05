package com.duchastel.simon.photocategorizer.auth

interface AuthProvider {
    fun executeWithAuthToken(execute: (authToken: AuthToken) -> Unit)
}

data class AuthToken(
    val accessToken: String
)