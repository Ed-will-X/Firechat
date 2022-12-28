package com.varsel.firechat.domain.use_case.message

import com.varsel.firechat.common.Resource
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class GetGroupRoomsRecurrentUseCase @Inject constructor(
    val repository: MessageRepository
) {
    operator fun invoke(): MutableStateFlow<Resource<List<GroupRoom>>> {
        return repository.getGroupRoomsRecurrent()
    }
}