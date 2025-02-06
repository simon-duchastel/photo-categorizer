package com.duchastel.simon.photocategorizer.screens.login

import android.app.PendingIntent
import androidx.lifecycle.ViewModel
import com.duchastel.simon.photocategorizer.auth.AuthProvider
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @Dropbox private val authProvider: AuthProvider,
): ViewModel() {
    private val _state: MutableStateFlow<State> = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    init {
        _state.update { oldState -> oldState.copy(isLoggedIn = authProvider.isLoggedIn()) }
    }

    fun login(redirectIntent: PendingIntent) {
        authProvider.login(redirectIntent)
    }

    fun logout() {
        authProvider.logout()
    }

    data class State(
        val isLoggedIn: Boolean? = null,
    )
}