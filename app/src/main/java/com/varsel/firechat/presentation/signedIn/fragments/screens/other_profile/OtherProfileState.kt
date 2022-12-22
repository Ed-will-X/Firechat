package com.varsel.firechat.presentation.signedIn.fragments.screens.other_profile

import com.varsel.firechat.data.local.User.User

data class OtherProfileState(
    val user: User?,
    val isLoading: Boolean = true,

)
