package com.varsel.firechat.model.message

import com.varsel.firechat.model.User.User

class Message {
//    var id: Long = 0L
    var messageUID: String? = null
    var message: String? = null
    var time: Long? = null
    var sender: String? = null
    var type: Int? = null
    var deleted: HashMap<String, String>? = null

    constructor(){

    }

    constructor(messageUID: String, message: String, time: Long, sender: String?, type: Int?){
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

class MessageStatus(){
    companion object {
        val SENT = 0
        val DELIVERED = 1
        val READ = 2
    }
}