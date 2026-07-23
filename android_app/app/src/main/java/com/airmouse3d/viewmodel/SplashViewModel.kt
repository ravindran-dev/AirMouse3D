package com.airmouse3d.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airmouse3d.repository.ConnectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * If the phone was already paired with a PC in a previous run, proactively reopens the UDP
 * socket to it here so the Home screen can start showing live reachability the instant it
 * appears, rather than waiting for the user to press Start first.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
) : ViewModel() {

    private val _navigateToHome = MutableStateFlow(false)
    val navigateToHome: StateFlow<Boolean> = _navigateToHome.asStateFlow()

    init {
        viewModelScope.launch {
            val minimumSplashMs = launch { delay(MIN_SPLASH_DURATION_MS) }

            runCatching { connectionRepository.ensureConnected() }

            minimumSplashMs.join()
            _navigateToHome.value = true
        }
    }

    companion object {
        private const val MIN_SPLASH_DURATION_MS = 900L
    }
}
