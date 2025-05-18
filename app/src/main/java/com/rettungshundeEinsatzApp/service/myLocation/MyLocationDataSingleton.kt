package com.rettungshundeEinsatzApp.service.myLocation

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object MyLocationData {
    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?> = _location
    private val _locationMGRS = MutableLiveData<String>()
    private val _locationTime = MutableLiveData<String>()

    fun updateLocation(newLocation: Location) {
        _location.postValue(newLocation)
    }

    fun updateLocationMGRS(newMessage: String) {
        _locationMGRS.postValue(newMessage)
    }

    fun updateLocationTime(newMessage: String) {
        _locationTime.postValue(newMessage)
    }


}

