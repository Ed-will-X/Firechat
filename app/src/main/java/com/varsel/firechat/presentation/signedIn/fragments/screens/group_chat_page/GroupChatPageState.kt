package com.varsel.firechat.presentation.signedIn.fragments.screens.group_chat_page

import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User

data class GroupChatPageState(
    val groupRooms: List<GroupRoom> = listOf(),
    val selectedRoom: GroupRoom? = null,
    val isLoading: Boolean = true
)
