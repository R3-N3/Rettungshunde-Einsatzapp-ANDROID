package com.rettungshundeEinsatzApp.service.myLocation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object MyLocationStatus {
    private val _gpsActive = MutableStateFlow(false)
    val gpsActive: StateFlow<Boolean> = _gpsActive

    fun setGpsActive(active: Boolean) {
        _gpsActive.value = active
    }
}