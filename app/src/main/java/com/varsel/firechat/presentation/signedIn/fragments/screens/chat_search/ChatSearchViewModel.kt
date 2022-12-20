package com.varsel.firechat.presentation.signedIn.fragments.screens.chat_search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatSearchViewModel: ViewModel() {
    val searchables = MutableLiveData<MutableList<Any>>(mutableListOf())

    val results = MutableLiveData<MutableList<Any>>(mutableListOf())


}