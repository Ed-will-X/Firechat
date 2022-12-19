package com.varsel.firechat.presentation.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatPageViewModel: ViewModel() {
    val actionBarVisibility = MutableLiveData<Boolean>(false)

    fun toggleActionbarVisibility(){
        actionBarVisibility.value = actionBarVisibility.value?.not()
    }
}