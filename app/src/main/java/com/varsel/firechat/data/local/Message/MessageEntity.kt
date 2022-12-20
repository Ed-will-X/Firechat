package com.varsel.firechat.data.local.Message

class MessageEntity {
//    var id: Long = 0L
    var messageUID: String? = null
    lateinit var message: String
    var time: Long = 0L
    lateinit var sender: String
    var type: Int = MessageType.TEXT
    var deleted: HashMap<String, String>? = null

    constructor(){

    }

    constructor(messageUID: String, message: String, time: Long, sender: String, type: Int){
        this.messageUID = messageUID
        this.message = message
        this.sender = sender
        this.time = time
        this.type = type
    }
}

class MessageType(){
    companion object {
        val TEXT = 0
        val IMAGE = 1
    }
}

class MessageStatus {
    companion object {
        val SENT = 0
        val RECEIVED = 1
        val SYSTEM = 2
    }
}

class SystemMessageType(){
    companion object {
        val GROUP_CREATE = "GROUP_CREATE"
        val NOW_ADMIN = "NOW_ADMIN"
        val NOT_ADMIN = "NOT_ADMIN"
        val GROUP_REMOVE = "GROUP_REMOVE"
        val GROUP_EXIT = "GROUP_EXIT"
        val GROUP_ADD = "GROUP_ADD"
    }
}