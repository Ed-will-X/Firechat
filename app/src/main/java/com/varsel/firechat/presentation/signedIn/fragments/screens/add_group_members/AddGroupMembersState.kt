package com.varsel.firechat.presentation.signedIn.fragments.screens.add_group_members

import com.varsel.firechat.data.local.Chat.GroupRoom

data class AddGroupMembersState(
    val groupRooms: List<GroupRoom> = listOf(),
    val selectedGroup: GroupRoom? = null
)