package com.varsel.firechat.presentation.signedIn.fragments.screens.add_friends

import com.varsel.firechat.data.local.User.User

data class AddFriendsState(
    val isLoading: Boolean = false,
    val isFieldEmpty: Boolean = true,
    val users: List<User> = listOf(),
    val textCount: Int = 0,

)
