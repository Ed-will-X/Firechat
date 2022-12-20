package com.varsel.firechat.domain.model

import com.varsel.firechat.data.local.Message.MessageEntity

data class ChatRoom(
    val roomUID: String,
    val participants: HashMap<String, String> = hashMapOf(),
    val messages: HashMap<String, MessageEntity>? = null
)
