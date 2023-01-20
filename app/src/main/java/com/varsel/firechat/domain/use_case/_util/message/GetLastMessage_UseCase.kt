package com.varsel.firechat.domain.use_case._util.message

import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Message.Message
import javax.inject.Inject

class GetLastMessage_UseCase @Inject constructor(
    val sortMessages: SortMessages_UseCase
) {
    operator fun invoke(chatRoom: ChatRoom): Message? {
        val sortedMessages: List<Message>? = sortMessages(chatRoom)

        return (sortedMessages?.last())
    }
}