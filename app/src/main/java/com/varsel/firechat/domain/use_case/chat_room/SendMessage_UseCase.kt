package com.varsel.firechat.domain.use_case.chat_room

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendMessage_UseCase @Inject constructor(
    val messageRepository: MessageRepository
) {
    operator fun invoke(message: Message, chatRoomId: String): Flow<Response> {
        return messageRepository.sendMessage(message, chatRoomId)
    }
}