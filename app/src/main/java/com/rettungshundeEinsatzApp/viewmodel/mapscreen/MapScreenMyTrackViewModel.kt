package com.rettungshundeEinsatzApp.viewmodel.mapscreen

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
import androidx.compose.runtime.State


class MapScreenMyTrackViewModel(private val locationDao: MyLocationDao) : ViewModel() {

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

    init {
        viewModelScope.launch {
            locationDao.getAllLocationsFlow().collectLatest { list ->
                _locations.value = list
            }
        }
    }

    class Factory(private val dao: MyLocationDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapScreenMyTrackViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapScreenMyTrackViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}