package com.varsel.firechat.domain.use_case._util.message

import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Message.Message

class SortMessages_UseCase {
    operator fun invoke(chatRoom: ChatRoom?): List<Message>?{
        val sorted = chatRoom?.messages?.values?.sortedBy {
            it.time
        }

        return sorted
    }
}