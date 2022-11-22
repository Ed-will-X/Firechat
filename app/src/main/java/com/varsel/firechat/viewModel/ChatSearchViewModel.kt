package com.varsel.firechat.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.User.User

class ChatSearchViewModel: ViewModel() {
    val searchables = MutableLiveData<MutableList<Any>>(mutableListOf())

    val results = MutableLiveData<MutableList<Any>>(mutableListOf())


}