package com.varsel.firechat.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.varsel.firechat.model.User.User

class FriendListFragmentViewModel: ViewModel() {
    val isSearchBarVisible = MutableLiveData<Boolean>(false)

    fun setSearchBarVisibility(value: Boolean){
        isSearchBarVisible.value = value
    }

    fun toggleSearchBarVisible(){
        isSearchBarVisible.value = !isSearchBarVisible.value!!
    }
}