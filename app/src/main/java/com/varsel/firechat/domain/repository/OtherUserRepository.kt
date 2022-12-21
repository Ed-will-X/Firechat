package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import kotlinx.coroutines.flow.Flow

// TODO: Add after callback for get requests
interface OtherUserRepository {

    fun getUserById(id: String) : Flow<User>
    suspend fun getUserSingle(id: String) : User   // for single occurrences like recycler views
    fun queryUsers(queryString: String) : Flow<List<User>>
    suspend fun sendFriendRequest(user: User) : Response
    suspend fun revokeFriendRequest(user: User) : Response
    suspend fun acceptFriendRequest(user: User) : Response
    suspend fun rejectFriendRequest(user: User) : Response
    suspend fun unfriendUser(user: User) : Response

}