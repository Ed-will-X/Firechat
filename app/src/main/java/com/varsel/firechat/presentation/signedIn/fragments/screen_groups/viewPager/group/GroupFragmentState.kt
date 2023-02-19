package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.group

import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User

data class GroupFragmentState(
    val currentUser: User? = null,
    val isLoading: Boolean = false
)