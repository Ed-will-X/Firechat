package com.varsel.firechat.domain.use_case.group_chat

import com.varsel.firechat.common.Resource
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.repository.MessageRepositoryImpl
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetGroupChatRecurrentUseCase @Inject constructor(
    val messageRepository: MessageRepository
) {
    operator fun invoke(id: String): MutableStateFlow<Resource<GroupRoom>> {
        val chatRooms = messageRepository.getGroupRoomsRecurrent().value.data

        chatRooms?.map {
            if(it.roomUID == id) {
                return MutableStateFlow(Resource.Success(it))
            }
        }

        return MutableStateFlow(Resource.Error("Could not find chat room"))
    }
}