package com.rettungshundeEinsatzApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rettungshundeEinsatzApp.database.area.AreaDao
import com.rettungshundeEinsatzApp.database.area.AreaWithCoordinates
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AreaViewModel(private val areaDao: AreaDao) : ViewModel() {

    val areas: StateFlow<List<AreaWithCoordinates>> = areaDao
        .getAllAreasWithCoordinatesFlow() // <-- Funktion im DAO, die Flow zurückgibt
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.Lazily,
            initialValue = emptyList()
        )

    class Factory(private val areaDao: AreaDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AreaViewModel(areaDao) as T
        }
    }


}