package com.rettungshundeEinsatzApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataDao
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataEntity
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserLocationsDao
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUsersLocationsEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AllTracksViewModel(
    locationDao: AllUserLocationsDao,
    userDao: AllUserDataDao
) : ViewModel() {

    val allLocations: StateFlow<List<AllUsersLocationsEntity>> =
        locationDao.getAllAsFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allUsers: StateFlow<List<AllUserDataEntity>> =
        userDao.getAllAsFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    class Factory(
        private val locationDao: AllUserLocationsDao,
        private val userDao: AllUserDataDao
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AllTracksViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AllTracksViewModel(locationDao, userDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}