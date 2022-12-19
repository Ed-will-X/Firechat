package com.varsel.firechat.data.local.ReadReceipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ReadReceiptViewModelFactory(private val dao: ReadReceiptDao): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ReadReceiptViewModel::class.java)){
            return ReadReceiptViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}