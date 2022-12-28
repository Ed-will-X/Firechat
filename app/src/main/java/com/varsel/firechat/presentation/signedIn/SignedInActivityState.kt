package com.varsel.firechat.presentation.signedIn

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User

data class SignedInActivityState(
    val currentUser: User? = null,
    val isLoading: Boolean = true
)
