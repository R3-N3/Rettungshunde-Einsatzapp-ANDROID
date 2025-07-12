package com.rettungshundeEinsatzApp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rettungshundeEinsatzApp.database.mylocallocation.MyLocationDao
import com.rettungshundeEinsatzApp.database.mylocallocation.MyLocationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.pow

class MyTrackViewModel(private val locationDao: MyLocationDao) : ViewModel() {

    private val _mapCenteredOnce = mutableStateOf(false)
    val mapCenteredOnce: State<Boolean> = _mapCenteredOnce

    fun resetCentering() {
        _mapCenteredOnce.value = false
    }

    fun markAsCentered() {
        _mapCenteredOnce.value = true
    }

    private val _locations = MutableStateFlow<List<MyLocationEntity>>(emptyList())
    val locations: StateFlow<List<MyLocationEntity>> = _locations

    private val _trackStats = MutableStateFlow(MyTrackStats(0, 0.0))
    val trackStats: StateFlow<MyTrackStats> = _trackStats

    init {
        viewModelScope.launch {
            locationDao.getAllLocationsFlow().collectLatest { list ->
                _locations.value = list
                _trackStats.value = calculateStats(list)
            }
        }
    }

    class Factory(private val dao: MyLocationDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyTrackViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MyTrackViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class MyTrackStats(
    val pointCount: Int,
    val totalDistanceMeters: Double
)
private fun calculateStats(locations: List<MyLocationEntity>): MyTrackStats {
    var distance = 0.0
    for (i in 1 until locations.size) {
        val prev = locations[i - 1]
        val curr = locations[i]
        distance += haversine(
            prev.latitude, prev.longitude,
            curr.latitude, curr.longitude
        )
    }
    return MyTrackStats(
        pointCount = locations.size,
        totalDistanceMeters = distance
    )
}

private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2).pow(2.0) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2).pow(2.0)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c
}
