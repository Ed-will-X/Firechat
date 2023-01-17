package com.varsel.firechat.domain.use_case.chat_room

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppendParticipants_UseCase @Inject constructor(
    val repository: MessageRepository
) {
    operator fun invoke(chatRoom: ChatRoom): Flow<Response> {
        return repository.appendParticipants(chatRoom)
    }
}