package com.varsel.firechat.domain.use_case.chat_room

import com.varsel.firechat.data.local.Chat.ChatRoom

class FindChatRoom_UseCase {
    operator fun invoke(userId: String, chatRooms: List<ChatRoom>): ChatRoom? {
        // TODO: Fix potential null pointer exception
        for(i in chatRooms){
            if(i.participants.contains(userId)){
                return i
            }
        }

        return null
    }
}