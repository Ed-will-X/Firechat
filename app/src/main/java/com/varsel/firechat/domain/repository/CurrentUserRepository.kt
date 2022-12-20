package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.UserEntity
import com.varsel.firechat.domain.model.User

// TODO: Add after callback for get requests
interface CurrentUserRepository {

    suspend fun getCurrentUserSingle(): User
    suspend fun getCurrentUserRecurrent(): User
    suspend fun signUp(name: String, email: String, password: String) : Response
    suspend fun signIn(email: String, password: String) : Response
    suspend fun editUser(key: String, value: String) : Response
    suspend fun addToRecentSearch(userId: String) : Response
    suspend fun deleteRecentSearchHistory() : Response

    fun signOut(after: ()-> Unit)

    suspend fun addGroupToFavorites(groupId: String) : Response
    suspend fun removeGroupFromFavorites(groupId: String) : Response
}