package com.varsel.firechat.data.local.Chat

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.varsel.firechat.data.local.Message.Message

@Entity(tableName = "chat")
open class ChatRoom {
    @PrimaryKey(autoGenerate = true)
//    var roomId: Long = 0L

    @ColumnInfo(name = "room_uid")
    lateinit var roomUID: String

    var participants: HashMap<String, String> = hashMapOf()

    var messages: HashMap<String, Message>? = null

    // For deletion, id will be added to this list to prevent redownload
    // then it will be removed from room

    constructor(){

    }

    constructor(roomUID: String, participants: HashMap<String, String>){
        this.roomUID = roomUID
        this.participants = participants
    }
}