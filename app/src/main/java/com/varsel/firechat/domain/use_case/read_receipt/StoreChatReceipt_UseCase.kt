package com.varsel.firechat.domain.use_case.read_receipt

import android.util.Log
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.domain.repository.MessageRepository
import com.varsel.firechat.domain.use_case._util.message.HasBeenRead_UseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StoreChatReceipt_UseCase @Inject constructor(
    val repository: MessageRepository,
    val checkReceipt: HasBeenRead_UseCase
) {
    operator fun invoke(message: Message, chatRoom: ChatRoom): Flow<Response> {
        val exists = checkReceipt(chatRoom, message)
        if(!exists) {
//            Log.d("RECEIPT", "Doesn't exist")
//            Log.d("RECEIPT", "Read By: ${message.readBy.keys}")
            return repository.storeReceipt_chatRoom(message, chatRoom.roomUID)
        }
//        Log.d("RECEIPT", "exists")
//        Log.d("RECEIPT", "Read By: ${message.readBy.keys}")

        return flow {
            emit(Response.Fail())
        }
    }
}