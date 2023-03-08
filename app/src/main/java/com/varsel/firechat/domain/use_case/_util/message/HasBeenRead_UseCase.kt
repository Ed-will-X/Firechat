package com.varsel.firechat.domain.use_case._util.message

import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserIdUseCase
import javax.inject.Inject

class HasBeenRead_UseCase @Inject constructor(
    val getCurrentUserId: GetCurrentUserIdUseCase
) {
    operator fun invoke(chatRoom: ChatRoom, message: Message): Boolean {
        val messages = chatRoom.messages?.values ?: listOf()

        for(i: Message in messages) {
            if(i.messageUID == message.messageUID) {
                i.readBy.forEach {
                    if(getCurrentUserId() == it.key) {
                        return true
                    }
                }
                break
            }
        }

        return false
    }
}