package com.varsel.firechat.model.PublicPost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PublicPostViewModelFactory(private val dao: PublicPostDao): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PublicPostViewModel::class.java)){
            return PublicPostViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}