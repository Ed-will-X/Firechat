package com.varsel.firechat.presentation.signedIn.fragments.screens.group_chat_detail

import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage

data class GroupChatDetailState(
    val selectedGroup: GroupRoom? = null,
    val groupRooms: List<GroupRoom> = listOf(),
    val isLoading: Boolean = true,
    val groupImage: ProfileImage? = null

)