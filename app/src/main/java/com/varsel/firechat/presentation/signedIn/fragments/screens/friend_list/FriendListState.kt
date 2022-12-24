package com.varsel.firechat.presentation.signedIn.fragments.screens.friend_list

import com.varsel.firechat.data.local.User.User

data class FriendListState(
    val friends: List<User> = listOf(),
    val isLoading: Boolean = true,
    val currentUser: User? = null
)