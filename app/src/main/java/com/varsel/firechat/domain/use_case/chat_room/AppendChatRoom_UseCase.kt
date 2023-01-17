package com.varsel.firechat.domain.use_case.chat_room

import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppendChatRoom_UseCase @Inject constructor(
    val messageRepository: MessageRepository
) {
    operator fun invoke(roomId: String, otherUser: String): Flow<Response> {
        return messageRepository.appendChatroomToUser(roomId, otherUser)
    }
}