package com.varsel.firechat.data.local.Image

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: Delete
//@HiltViewModel
//class ImageViewModel @Inject constructor(
//    val dao: ImageDao
//): ViewModel() {
//
//    val imageToExpand = MutableLiveData<String?>()
//    val showProfileImage = MutableLiveData<Boolean>(false)
//    val username = MutableLiveData<String?>()
//    val type = MutableLiveData<String?>()
//    val timestamp = MutableLiveData<Long>(0L)
//    val image = MutableLiveData<String?>()
//
//    fun setShowProfileImage(bool: Boolean){
//        showProfileImage.value = bool
//    }
//
//    fun setOverlayProps(username: String, type: String, timestamp: Long, image: String){
//        this.username.value = username
//        this.type.value = type
//        this.timestamp.value = timestamp
//        this.image.value = image
//    }
//
//    fun clearOverlayProps(){
//        this.username.value = null
//        this.type.value = null
//        this.timestamp.value = 0L
//        this.image.value = null
//    }
//
//    fun getImageById(id: String): LiveData<Image> {
//        val image = dao.get_liveData(id)
//        return image
//    }
//
//    fun storeImage(image: Image){
//        viewModelScope.launch {
//            dao.insert(image)
//        }
//    }
//
//    fun storeImage(image: Image, afterCallback: ()-> Unit){
//        viewModelScope.launch {
//            dao.insert(image)
//            afterCallback()
//        }
//    }
//
//    fun deleteImage(image: Image){
//        viewModelScope.launch {
//            dao.delete(image)
//        }
//    }
//
//    fun checkForImgInRoom(imageId: String): LiveData<Image> {
//        val imageLiveData = dao.get_liveData(imageId)
//        return imageLiveData
//    }
//}