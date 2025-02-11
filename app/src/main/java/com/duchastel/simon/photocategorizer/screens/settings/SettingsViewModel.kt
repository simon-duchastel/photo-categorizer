package com.duchastel.simon.photocategorizer.screens.settings

import androidx.lifecycle.ViewModel
import com.duchastel.simon.photocategorizer.auth.AuthManager
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @Dropbox private val authManager: AuthManager,
) : ViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(State)
    val state: StateFlow<State> = _state

    fun onLogoutClicked() {
        authManager.logout()
    }

    data object State
}