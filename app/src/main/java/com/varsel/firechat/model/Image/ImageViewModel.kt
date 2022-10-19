package com.varsel.firechat.model.Image

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ImageViewModel(val dao: ImageDao): ViewModel() {
    val profileImage = MutableLiveData<Image>()
    val profileImageEncoded = MutableLiveData<String>()

    fun getImageById(id: String): LiveData<Image> {
        val image = dao.get(id)
        return image
    }

    fun storeImage(image: Image){
        Log.d("LLL", "Store Image Called :::::::::::::::::::::::::::::::::: ${image.ownerId}")
        viewModelScope.launch {
            dao.insert(image)
        }
    }

    fun deleteImage(image: Image){
        viewModelScope.launch {
            dao.delete(image)
        }
    }

    fun checkForProfileImageInRoom(ownerId: String): LiveData<Image>?{
        val image = dao.get(ownerId)
        return image
    }

    fun determineImageRetrieveMethod(){

    }

}