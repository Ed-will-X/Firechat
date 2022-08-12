package com.varsel.firechat.model.currentUser

import androidx.room.Entity
import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.User.User

@Entity(tableName = "current_user")
class CurrentUser: User {
    var chatRooms: MutableList<ChatRoom>? = null
    var groupRooms: MutableList<ChatRoom>? = null

    constructor(){

    }

    constructor(name: String, email: String, about: String, userUID: String, phone: String, profileImage: Array<Byte>, profileLock: Boolean){
        super.name = name
        super.phone = phone
        super.about = about
        super.userUID = userUID
        super.phone = phone
        super.profileImage = profileImage
        super.profileLock = profileLock
    }

}