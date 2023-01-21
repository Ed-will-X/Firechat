package com.varsel.firechat.presentation.signedIn.fragments.screens.about_user

import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User

data class AboutUserState(
    val selectedUser: User? = null,
    val profileImage: ProfileImage? = null
)
