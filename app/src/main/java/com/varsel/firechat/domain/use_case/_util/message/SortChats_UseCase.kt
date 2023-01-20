package com.varsel.firechat.domain.use_case._util.message

import com.varsel.firechat.data.local.Chat.ChatRoom
import javax.inject.Inject

class SortChats_UseCase @Inject constructor(
    val sortMessages: SortMessages_UseCase
) {
    operator fun invoke(chatRooms: List<ChatRoom>?): MutableList<ChatRoom>?{
        val sorted = chatRooms?.sortedBy {
            val sortedMessages = sortMessages(it)

            sortedMessages?.last()?.time
        }?.reversed()?.toMutableList()

        return sorted
    }
}