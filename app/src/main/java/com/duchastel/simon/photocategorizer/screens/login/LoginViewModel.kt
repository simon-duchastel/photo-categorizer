package com.duchastel.simon.photocategorizer.screens.login

import android.app.PendingIntent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duchastel.simon.photocategorizer.auth.AuthProvider
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @Dropbox private val authProvider: AuthProvider,
): ViewModel() {
    private val _state: MutableStateFlow<State> = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    init {
        _state.update { oldState -> oldState.copy(isLoggedIn = authProvider.isLoggedIn()) }
        viewModelScope.launch {
            authProvider
                .isLoggedInFlow()
                .distinctUntilChanged()
                .collect { isLoggedIn ->
                    _state.update { oldState -> oldState.copy(isLoggedIn = isLoggedIn) }
                }
        }
    }

    fun login(redirectIntent: PendingIntent) {
        viewModelScope.launch {
            _state.update { oldState -> oldState.copy(loginInProgress = true) }
            authProvider.login(redirectIntent)
            _state.update { oldState -> oldState.copy(loginInProgress = false) }
        }
    }

    fun logout() {
        authProvider.logout()
    }

    data class State(
        val isLoggedIn: Boolean? = null,
        val loginInProgress: Boolean = false,
    )
}