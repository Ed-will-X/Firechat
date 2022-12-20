package com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatPageViewModel: ViewModel() {
    val actionBarVisibility = MutableLiveData<Boolean>(false)

    fun toggleActionbarVisibility(){
        actionBarVisibility.value = actionBarVisibility.value?.not()
    }
}