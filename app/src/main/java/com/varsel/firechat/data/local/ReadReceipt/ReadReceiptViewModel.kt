package com.varsel.firechat.data.local.ReadReceipt

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadReceiptViewModel @Inject constructor(
    val dao: ReadReceiptDao
): ViewModel() {

    fun storeReceipt(receipt: ReadReceiptEntity){
        viewModelScope.launch {
            dao.insert(receipt)
        }
    }

    fun fetchReceipt(id: String): LiveData<ReadReceiptEntity> {
        val receipt = dao.get(id)
        return receipt
    }

}