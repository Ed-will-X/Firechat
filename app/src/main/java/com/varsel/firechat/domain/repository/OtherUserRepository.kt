package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.UserEntity
import com.varsel.firechat.domain.model.User

// TODO: Add after callback for get requests
interface OtherUserRepository {

    suspend fun getUserById(id: String) : User
    suspend fun getUserSingle(id: String) : User   // for single occurrences like recycler views
    suspend fun queryUsers(queryString: String) : List<User>
    suspend fun sendFriendRequest(user: UserEntity) : Response
    suspend fun revokeFriendRequest(user: UserEntity) : Response
    suspend fun acceptFriendRequest(user: UserEntity) : Response
    suspend fun rejectFriendRequest(user: UserEntity) : Response
    suspend fun unfriendUser(user: UserEntity) : Response

}