package com.varsel.firechat.domain.use_case.message

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteMessage_Group_UseCase @Inject constructor(
    val messageRepository: MessageRepository
) {
    operator fun invoke(message: Message, groupRoomId: String): Flow<Response> {
        return messageRepository.deleteMessage_groupRoom(message, groupRoomId)
    }
}