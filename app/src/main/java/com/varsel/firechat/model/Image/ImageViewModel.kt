package com.varsel.firechat.model.Image

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.model.User.User
import kotlinx.coroutines.launch

class ImageViewModel(val dao: ImageDao): ViewModel() {
    val profileImage = MutableLiveData<Image>()

    fun getImageById(id: String): LiveData<Image> {
        val image = dao.get(id)
        return image
    }

    fun storeImage(image: Image){
        viewModelScope.launch {
            dao.insert(image)
        }
    }

    fun getProfileImage(imageId: String){
        viewModelScope.launch {
            val image = dao.get(imageId)
            profileImage.value = image.value
        }
    }

    fun determineImageRetrieveMethod(user: User): LiveData<Image>?{
        if(user.profileImageId != null){
            val image = dao.get(user.profileImageId!!)
            Log.d("LLL", "PP ID in user: ${image.value?.ownerId}")
            return image
        } else {
            return null
        }
    }
}