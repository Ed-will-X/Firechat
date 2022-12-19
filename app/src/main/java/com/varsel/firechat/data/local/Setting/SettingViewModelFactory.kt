package com.varsel.firechat.data.local.Setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SettingViewModelFactory(private val dao: SettingDao): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SettingViewModel::class.java)){
            return SettingViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}