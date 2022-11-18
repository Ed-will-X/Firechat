package com.varsel.firechat.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddGroupMembersViewModel: ViewModel() {
    val hasBeenClicked = MutableLiveData<Boolean>(false)
}