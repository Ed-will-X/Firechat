package com.varsel.firechat.domain.repository.remote

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.dto.UserDto

// TODO: Add after callback for get requests
interface OtherUserRepository {

    suspend fun getUserById(id: String) : UserDto
    suspend fun getUserSingle(id: String) : UserDto   // for single occurrences like recycler views
    suspend fun queryUsers(queryString: String) : List<UserDto>
    suspend fun sendFriendRequest(user: User) : Response
    suspend fun revokeFriendRequest(user: User) : Response
    suspend fun acceptFriendRequest(user: User) : Response
    suspend fun rejectFriendRequest(user: User) : Response
    suspend fun unfriendUser(user: User) : Response

}