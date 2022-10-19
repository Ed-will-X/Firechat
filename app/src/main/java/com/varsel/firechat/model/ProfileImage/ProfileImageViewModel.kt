package com.varsel.firechat.model.ProfileImage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProfileImageViewModel(val dao: ProfileImageDao): ViewModel() {
    val profileImage = MutableLiveData<ProfileImage>()
    val profileImageEncoded = MutableLiveData<String>()

    fun getImageById(id: String): LiveData<ProfileImage> {
        val image = dao.get(id)
        return image
    }

    fun storeImage(profileImage: ProfileImage){
        Log.d("LLL", "Store Image Called :::::::::::::::::::::::::::::::::: ${profileImage.ownerId}")
        viewModelScope.launch {
            dao.insert(profileImage)
        }
    }

    fun deleteImage(profileImage: ProfileImage){
        viewModelScope.launch {
            dao.delete(profileImage)
        }
    }

    fun checkForProfileImageInRoom(ownerId: String): LiveData<ProfileImage>?{
        val image = dao.get(ownerId)
        return image
    }

    fun determineImageRetrieveMethod(){

    }

}