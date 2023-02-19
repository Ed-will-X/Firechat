package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.viewPager.Individual

import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.User.User

data class IndividualFragmentState(
    val isLoading: Boolean = true,
    val currentUser: User? = null
)