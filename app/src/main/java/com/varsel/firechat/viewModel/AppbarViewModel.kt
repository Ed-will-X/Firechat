package com.varsel.firechat.viewModel

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class AppbarTags {
    CHATS,
    PROFILE
}

class AppbarViewModel: ViewModel() {
    val selectedPage = MutableLiveData<AppbarTags>()

    fun setPage(tag: AppbarTags){
        if (tag == AppbarTags.CHATS){
            selectedPage.value = AppbarTags.CHATS
            Log.d("PAGE", "CHATS PAGE")
        }
        if(tag == AppbarTags.PROFILE){
            selectedPage.value = AppbarTags.PROFILE
            Log.d("PAGE", "PROFILE PAGE")
        }
    }
}