package com.varsel.firechat.domain.use_case.group_chat

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RemoveFromGroup_UseCase @Inject constructor(
    val messageRepository: MessageRepository
) {
    operator fun invoke(userId: String, groupRoom: GroupRoom): Flow<Response> {
        return messageRepository.removeFromGroup(userId, groupRoom)
    }
}