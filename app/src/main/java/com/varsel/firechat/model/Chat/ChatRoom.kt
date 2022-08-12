package com.varsel.firechat.model.Chat

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.varsel.firechat.model.User.User
import com.varsel.firechat.model.message.Message

@Entity(tableName = "chat")
class ChatRoom {
    @PrimaryKey(autoGenerate = true)
    var roomId: Long = 0L

    @ColumnInfo(name = "room_uid")
    var roomUID: String? = null

    var participants: MutableList<String>? = null

    var messages: MutableList<Message>? = null

    var deleted: MutableList<String>? = null
    // For deletion, id will be added to this list to prevent redownload
    // then it will be removed from room

    constructor(){

    }

    constructor(roomUID: String, participants: MutableList<String>, messages: MutableList<Message>){
        this.roomUID = roomUID
        this.participants = participants
        this.messages = messages
    }
}