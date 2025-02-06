package com.duchastel.simon.photocategorizer.screens.login

import android.app.PendingIntent
import android.content.Context
import androidx.lifecycle.ViewModel
import com.duchastel.simon.photocategorizer.MainActivity
import com.duchastel.simon.photocategorizer.auth.AuthProvider
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext context: Context,
    @Dropbox private val authProvider: AuthProvider,
): ViewModel() {
    private val _state: MutableStateFlow<State> = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val redirectIntent = PendingIntent.getActivity(
        /* context = */ context,
        /* requestCode = */ 0,
        /* intent = */ MainActivity.createIntent(context),
        /* flags = */ PendingIntent.FLAG_MUTABLE,
    )

    init {
        _state.update { oldState -> oldState.copy(isLoggedIn = authProvider.isLoggedIn()) }
    }

    fun login() {
        authProvider.login(redirectIntent)
    }

    data class State(
        val isLoggedIn: Boolean? = null,
    )
}