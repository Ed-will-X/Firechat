package com.varsel.firechat.model.ReadReceipt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.ProfileImage.ProfileImage
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