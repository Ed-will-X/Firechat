package com.varsel.firechat.domain.use_case.message

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateGroupUseCase @Inject constructor(
    val repository: MessageRepository
) {
    operator fun invoke(group: GroupRoom): Flow<Response> {
        return repository.createGroup(group)
    }
}