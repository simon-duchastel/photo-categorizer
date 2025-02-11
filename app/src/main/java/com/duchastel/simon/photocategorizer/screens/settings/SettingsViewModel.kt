package com.duchastel.simon.photocategorizer.screens.settings

import androidx.lifecycle.ViewModel
import com.duchastel.simon.photocategorizer.auth.AuthRepository
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import com.duchastel.simon.photocategorizer.storage.LocalStorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @Dropbox private val authRepository: AuthRepository,
    private val localStorage: LocalStorageRepository,
) : ViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(State)
    val state: StateFlow<State> = _state

    fun onLogoutClicked() {
        authRepository.logout()
    }

    data object State
}