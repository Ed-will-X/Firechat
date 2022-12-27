package com.varsel.firechat.presentation.signedIn.fragments.screens.create_group

import com.varsel.firechat.data.local.User.User

data class CreateGroupState(
    val friends: List<User>? = null,
    val currentUser: User? = null,
    val isLoading: Boolean = true
)