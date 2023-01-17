package com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page

import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.User.User

data class ChatPageState(
    val chatRooms: List<ChatRoom> = listOf(),
    val selectedChatRoom: ChatRoom? = null,
    val isLoading: Boolean = true
)