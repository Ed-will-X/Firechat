package com.varsel.firechat.presentation.signedIn.fragments.screens.add_friends

import com.varsel.firechat.data.local.User.User

sealed class AddFriendsEvent() {
    class OnRecentSearchClick(val user: User, val base64: String?): AddFriendsEvent()
    class OnResultClick(val user: User, val base64: String): AddFriendsEvent()
}
