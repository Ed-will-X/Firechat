package com.varsel.firechat.domain.use_case.group_chat

import android.widget.EditText
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.domain.repository.MessageRepository
import com.varsel.firechat.utils.MessageUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendGroupMessage_UseCase @Inject constructor(
    val messageRepository: MessageRepository,
    val currentUserRepository: CurrentUserRepository
) {
    operator fun invoke(roomId: String, messageEditText: EditText): Flow<Response> {
        val messageText = messageEditText.text.toString().trim()
        val currentUser = currentUserRepository.getCurrentUserId()

        val message = Message(MessageUtils.generateUID(30), messageText, System.currentTimeMillis(), currentUser, MessageType.TEXT)
        return messageRepository.sendGroupMessage(message, roomId)
    }

    operator fun invoke(roomId: String, message: Message): Flow<Response> {

        return messageRepository.sendGroupMessage(message, roomId)
    }
}