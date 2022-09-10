package com.varsel.firechat.model.Chat

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "group")
class GroupRoom: ChatRoom {

    @ColumnInfo(name = "group_name")
    var groupName: String? = null

    var admins: HashMap<String, String>? = null

    var about: String? = null

    var subject: String? = null

//    var groupIcon:

    constructor(){

    }

    constructor(roomUID: String, participants: HashMap<String, String>, groupName: String, admins: HashMap<String, String>){
        super.roomUID = roomUID
        super.participants = participants

        this.admins = admins
        this.groupName = groupName
    }
}