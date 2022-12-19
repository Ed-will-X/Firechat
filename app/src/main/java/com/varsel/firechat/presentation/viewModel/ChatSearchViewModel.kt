package com.varsel.firechat.presentation.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatSearchViewModel: ViewModel() {
    val searchables = MutableLiveData<MutableList<Any>>(mutableListOf())

    val results = MutableLiveData<MutableList<Any>>(mutableListOf())


}