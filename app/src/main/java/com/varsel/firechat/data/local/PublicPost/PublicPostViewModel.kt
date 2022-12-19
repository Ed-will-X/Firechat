package com.varsel.firechat.data.local.PublicPost

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PublicPostViewModel(val dao: PublicPostDao): ViewModel() {
    val currentUserPublicPosts = MutableLiveData<MutableList<PublicPost>?>(mutableListOf())

    fun getPostById(id: String): LiveData<PublicPost> {
        val post = dao.get(id)
        return post
    }

    fun storePost(publicPost: PublicPost){
        viewModelScope.launch {
            dao.insert(publicPost)
        }
    }

    fun deletePost(publicPost: PublicPost){
        viewModelScope.launch {
            dao.delete(publicPost)
        }
    }

    fun checkIfPostInRoom(ownerId: String): LiveData<PublicPost>{
        val post = dao.get(ownerId)
        return post
    }
}