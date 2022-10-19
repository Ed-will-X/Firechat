package com.varsel.firechat.model.ProfileImage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProfileImageViewModelFactory(private val dao: ProfileImageDao): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ProfileImageViewModel::class.java)){
            return ProfileImageViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}