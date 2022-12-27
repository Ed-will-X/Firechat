package com.varsel.firechat.domain.use_case.message

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppendGroupIdToUserUseCase @Inject constructor(
    val repository: MessageRepository
) {
    operator fun invoke(group: GroupRoom, user: String): Flow<Response> {
        return repository.appendGroupRoomsToUser(group, user)
    }
}