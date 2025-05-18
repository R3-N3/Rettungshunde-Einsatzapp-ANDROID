package com.rettungshundeEinsatzApp.viewmodel.userdata

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataEntity
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AllUserProfileViewModel(application: Application) : AndroidViewModel(application) {


    private val dao = AllUserDataProvider.getDatabase(application).allUserDataDao()

    val userList: StateFlow<List<AllUserDataEntity>> =
        dao.getAllAsFlow().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )


}