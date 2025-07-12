package com.rettungshundeEinsatzApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataDao
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataEntity
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserLocationsDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class UsersWithTracksViewModel(
    private val userDao: AllUserDataDao,
    private val locationDao: AllUserLocationsDao
) : ViewModel() {

    val usersWithTrackStats: StateFlow<List<UserTrackStats>> =
        combine(
            userDao.getAllUsersWithLocationsAsFlow(),
            locationDao.getAllAsFlow()
        ) { users, locations ->

            users.map { user ->
                val userLocations = locations
                    .filter { it.userId == user.id }
                    .sortedBy { it.timestamp }

                val trackCount = userLocations.size

                var totalDistance = 0.0
                for (i in 1 until userLocations.size) {
                    val prev = userLocations[i - 1]
                    val curr = userLocations[i]
                    totalDistance += distanceBetween(
                        prev.latitude, prev.longitude,
                        curr.latitude, curr.longitude
                    )
                }

                UserTrackStats(
                    user = user,
                    trackCount = trackCount,
                    totalDistanceMeters = totalDistance
                )
            }
        }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun distanceBetween(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    class Factory(
        private val userDao: AllUserDataDao,
        private val locationDao: AllUserLocationsDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UsersWithTracksViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UsersWithTracksViewModel(userDao, locationDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class UserTrackStats(
    val user: AllUserDataEntity,
    val trackCount: Int,
    val totalDistanceMeters: Double
)