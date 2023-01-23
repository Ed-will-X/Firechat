package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.profile

import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User

data class ProfileState(
    val isLoading: Boolean = true,
    val profileImage: ProfileImage? = null,
    val isConnectedToServer: Boolean = false
)