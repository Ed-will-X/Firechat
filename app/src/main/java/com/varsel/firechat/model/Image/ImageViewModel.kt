package com.varsel.firechat.model.Image

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ImageViewModel(val dao: ImageDao): ViewModel() {
    val profileImage = MutableLiveData<Image>()

    fun getImage(imageId: String){
        viewModelScope.launch {
            val image: LiveData<Image> = dao.get(imageId)

            profileImage.value = image.value
        }
    }

    fun insertImage(image: Image){
        viewModelScope.launch {
            dao.insert(image)
        }
    }

    fun removeImage(image: Image){
        viewModelScope.launch {
            dao.delete(image)
        }
    }

    fun removeImageById(id: String){
        viewModelScope.launch {
//            dao.deleteById(id)
        }
    }
}