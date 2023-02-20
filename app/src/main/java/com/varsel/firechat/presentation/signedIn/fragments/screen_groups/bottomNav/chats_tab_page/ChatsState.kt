package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.chats_tab_page

import com.varsel.firechat.data.local.User.User

data class ChatsState (
    val currentUser: User? = null,
    val currentUserImage: String? = null
)