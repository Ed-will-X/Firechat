package com.varsel.firechat.model.Image

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ImageViewModelFactory(private val dao: ImageDao): ViewModelProvider.Factory  {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ImageViewModel::class.java)){
            return ImageViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}