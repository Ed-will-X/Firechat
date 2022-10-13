package com.varsel.firechat.model.Setting

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch

//class SettingViewModel(private val dao: SettingDao) : ViewModel() {
//
//    fun getProfilePic(): LiveData<Setting> {
//        val returnValue = dao.get(SettingKey.PROFILE_PICTURE)
//
//        return returnValue
//    }
//
//    fun storeProfilePic(base64: String){
//        viewModelScope.launch {
//            dao.insert(Setting(SettingKey.PROFILE_PICTURE, base64))
//        }
//    }
//
//    fun deleteProfilePic(){
//        val returnValue = dao.get(SettingKey.PROFILE_PICTURE)
//
//        viewModelScope.launch {
//            if(returnValue != null){
//                Log.d("LLL", "Delete ran")
//                dao.insert(Setting(SettingKey.PROFILE_PICTURE, null))
//            }
//        }
//    }
//}