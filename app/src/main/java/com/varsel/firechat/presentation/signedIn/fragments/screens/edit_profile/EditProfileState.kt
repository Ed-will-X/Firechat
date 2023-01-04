package com.varsel.firechat.presentation.signedIn.fragments.screens.edit_profile

import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User

data class EditProfileState(
    val user: User?,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val profileImage: ProfileImage? = null
)
