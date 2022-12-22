package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import kotlinx.coroutines.flow.Flow

// TODO: Add after callback for get requests
interface OtherUserRepository {

    fun getUserById(id: String) : Flow<User>    // for single occurrences like recycler views
    fun getUserRecurrent(id: String) : Flow<User>
    fun queryUsers(queryString: String) : Flow<List<User>>
    fun sendFriendRequest(user: User) : Flow<Response>
    fun revokeFriendRequest(user: User) : Flow<Response>
    fun acceptFriendRequest(user: User) : Flow<Response>
    fun rejectFriendRequest(user: User) : Flow<Response>
    fun unfriendUser(user: User) : Flow<Response>

}