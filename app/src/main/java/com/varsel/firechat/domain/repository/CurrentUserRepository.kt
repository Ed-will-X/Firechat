package com.varsel.firechat.domain.repository

import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.dto.UserDto

// TODO: Add after callback for get requests
interface CurrentUserRepository {

    fun getCurrentUserSingle(after: (user: UserDto)-> Unit)
    fun getCurrentUserRecurrent(after: (user: UserDto)-> Unit)
    fun signUp(email: String, password: String, success: ()-> Unit, fail: ()-> Unit)
    fun signIn(email: String, password: String, success: ()-> Unit, fail: ()-> Unit)
    fun saveUser(user: User, success: () -> Unit, fail: () -> Unit)
    fun editUser(key: String, value: String)
    fun addToRecentSearch(userId: String, success: () -> Unit, fail: () -> Unit)
    fun deleteRecentSearchHistory(success: () -> Unit, fail: () -> Unit)

    fun signOut()

    fun addGroupToFavorites(groupId: String, success: () -> Unit, fail: () -> Unit)
    fun removeGroupFromFavorites(groupId: String, success: () -> Unit, fail: () -> Unit)
}