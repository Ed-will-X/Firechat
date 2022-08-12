package com.varsel.firechat.model.message

import com.varsel.firechat.model.User.User

class Message {
//    var id: Long = 0L
    var messageUID: String? = null
    var message: String? = null
    var time: Long? = null
    var sender: String? = null

    constructor(){

    }

    constructor(messageUID: String, message: String, time: Long, sender: String?){
        this.messageUID = messageUID
        this.message = message
        this.sender = sender
        this.time = time
    }
}