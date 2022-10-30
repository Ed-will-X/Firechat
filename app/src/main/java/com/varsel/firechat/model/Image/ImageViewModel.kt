package com.varsel.firechat.model.Image

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ImageViewModel(val dao: ImageDao): ViewModel() {
    val imageToExpand = MutableLiveData<String?>()

    fun getImageById(id: String): LiveData<Image> {
        val image = dao.get(id)
        return image
    }

    fun storeImage(image: Image){
        viewModelScope.launch {
            dao.insert(image)
        }
    }

    fun deleteImage(image: Image){
        viewModelScope.launch {
            dao.delete(image)
        }
    }

    fun checkForImgInRoom(imageId: String): LiveData<Image> {
        val imageLiveData = dao.get(imageId)
        return imageLiveData
    }
}