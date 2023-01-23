package com.varsel.firechat.domain.use_case.chat_room

import com.varsel.firechat.data.local.Chat.ChatRoom

/*  Checks if there is an existing chat room with the user in question
*
*/
class DoesChatRoomExist_UseCase {
    operator fun invoke(userId: String, chatRooms: List<ChatRoom>): Boolean{
        var contains: Boolean = false
        for(i in chatRooms){
            if(i.participants.contains(userId)){
                contains = true
                break
            }
        }
        return contains
    }
}