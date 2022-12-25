package com.varsel.firechat.domain.use_case.message

import com.varsel.firechat.common.Resource
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class GetChatRoomsRecurrentUseCase @Inject constructor(
    val messageRepository: MessageRepository
) {
    operator fun invoke(): MutableStateFlow<Resource<List<ChatRoom>>> {
        return messageRepository.getChatRoomsRecurrent()
    }
}