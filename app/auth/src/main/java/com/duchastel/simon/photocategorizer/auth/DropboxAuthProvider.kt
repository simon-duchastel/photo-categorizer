package com.duchastel.simon.photocategorizer.auth

import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

@Singleton
internal class DropboxAuthProvider @Inject @Dropbox constructor(): AuthProvider {
    override fun executeWithAuthToken(execute: (authToken: AuthToken) -> Unit) {
        execute(AuthToken("TODO"))
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Dropbox