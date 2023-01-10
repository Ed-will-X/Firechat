package com.varsel.firechat.domain.use_case.group_chat

import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EditGroupUseCase @Inject constructor(
    val messageRepository: MessageRepository
) {

    operator fun invoke(key: String, value: String, groupId: String): Flow<Response> {
        return messageRepository.editGroup(key, value, groupId)
    }
}