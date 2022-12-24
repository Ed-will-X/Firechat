package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.friends

import com.varsel.firechat.data.local.User.User

data class FriendsFragmentState(
    val isLoading: Boolean = true,
    val friends: List<User> = listOf()
)