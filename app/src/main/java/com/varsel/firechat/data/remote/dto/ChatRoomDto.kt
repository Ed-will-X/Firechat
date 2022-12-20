package com.varsel.firechat.data.remote.dto

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.varsel.firechat.data.local.Message.Message

data class ChatRoomDto(
    val roomUID: String,    // TODO: Remove if not needed
    val participants: HashMap<String, String> = hashMapOf(),
    val messages: HashMap<String, Message>? = null
)
