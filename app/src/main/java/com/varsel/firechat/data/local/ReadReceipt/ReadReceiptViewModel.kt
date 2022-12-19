package com.varsel.firechat.data.local.ReadReceipt

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ReadReceiptViewModel(val dao: ReadReceiptDao): ViewModel() {

    fun storeReceipt(receipt: ReadReceipt){
        viewModelScope.launch {
            dao.insert(receipt)
        }
    }

    fun fetchReceipt(id: String): LiveData<ReadReceipt> {
        val receipt = dao.get(id)
        return receipt
    }

}