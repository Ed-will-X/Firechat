package com.varsel.firechat.data.remote.dto

import com.varsel.firechat.data.local.Message.MessageEntity

data class ChatRoomDto(
    val roomUID: String,
    val participants: HashMap<String, String> = hashMapOf(),
    val messages: HashMap<String, MessageEntity>? = null
)
