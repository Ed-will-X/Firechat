package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User

// TODO: Add after callback for get requests
interface OtherUserRepository {

    suspend fun getUserById(id: String) : User
    suspend fun getUserSingle(id: String) : User   // for single occurrences like recycler views
    suspend fun queryUsers(queryString: String) : List<User>
    suspend fun sendFriendRequest(user: User) : Response
    suspend fun revokeFriendRequest(user: User) : Response
    suspend fun acceptFriendRequest(user: User) : Response
    suspend fun rejectFriendRequest(user: User) : Response
    suspend fun unfriendUser(user: User) : Response

}