package com.varsel.firechat.data.local.PublicPost

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PublicPostViewModel @Inject constructor(
    val dao: PublicPostDao
): ViewModel() {
    val currentUserPublicPosts = MutableLiveData<MutableList<PublicPostEntity>?>(mutableListOf())

    fun getPostById(id: String): LiveData<PublicPostEntity> {
        val post = dao.get(id)
        return post
    }

    fun storePost(publicPost: PublicPostEntity){
        viewModelScope.launch {
            dao.insert(publicPost)
        }
    }

    fun deletePost(publicPost: PublicPostEntity){
        viewModelScope.launch {
            dao.delete(publicPost)
        }
    }

    fun checkIfPostInRoom(ownerId: String): LiveData<PublicPostEntity>{
        val post = dao.get(ownerId)
        return post
    }
}