package com.varsel.firechat.domain.repository

import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.dto.UserDto

// TODO: Add after callback for get requests
interface OtherUserRepository {

    fun getUserById(id: String, after: (user: UserDto)-> Unit)
    fun getUserSingle(id: String, after: (user: UserDto)-> Unit)    // for single occurrences like recycler views
    fun queryUsers(queryString: String, after: (user: UserDto) -> Unit)
    fun sendFriendRequest(user: User, success: ()-> Unit, failure: ()-> Unit)
    fun revokeFriendRequest(user: User, success: ()-> Unit, failure: ()-> Unit)
    fun acceptFriendRequest(user: User, success: ()-> Unit, failure: ()-> Unit)
    fun rejectFriendRequest(user: User, success: ()-> Unit, failure: ()-> Unit)
    fun unfriendUser(user: User, success: ()-> Unit, failure: ()-> Unit)

}