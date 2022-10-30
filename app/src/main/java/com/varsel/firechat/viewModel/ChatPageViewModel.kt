package com.varsel.firechat.viewModel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatPageViewModel: ViewModel() {
    val actionBarVisibility = MutableLiveData<Boolean>(false)

    fun toggleActionbarVisibility(){
        actionBarVisibility.value = actionBarVisibility.value?.not()
    }
}