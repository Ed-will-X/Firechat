package com.varsel.firechat.domain.repository.remote

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.dto.UserDto

// TODO: Add after callback for get requests
interface CurrentUserRepository {

    suspend fun getCurrentUserSingle(): UserDto
    suspend fun getCurrentUserRecurrent(): UserDto
    suspend fun signUp(email: String, password: String) : Response
    suspend fun signIn(email: String, password: String) : Response
    suspend fun saveUser(user: User) : Response
    suspend fun editUser(key: String, value: String) : Response
    suspend fun addToRecentSearch(userId: String) : Response
    suspend fun deleteRecentSearchHistory() : Response

    fun signOut(after: ()-> Unit)

    suspend fun addGroupToFavorites(groupId: String) : Response
    suspend fun removeGroupFromFavorites(groupId: String) : Response
}