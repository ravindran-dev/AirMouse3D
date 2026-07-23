package com.airmouse3d.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airmouse3d.model.PcAddress
import com.airmouse3d.repository.ConnectionRepository
import com.airmouse3d.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
) : ViewModel() {

    private val _paired = MutableStateFlow(false)
    val paired: StateFlow<Boolean> = _paired.asStateFlow()

    private var pairing = false

    /** Called once per detected QR frame; only the first valid one actually pairs. */
    fun onQrScanned(raw: String) {
        if (pairing) return
        val address = PcAddress.parse(raw, Constants.DEFAULT_PC_UDP_PORT) ?: return
        pairing = true
        viewModelScope.launch {
            connectionRepository.pairWith(address)
            _paired.value = true
        }
    }
}
