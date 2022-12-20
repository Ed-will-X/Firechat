package com.varsel.firechat.data.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.UserEntity
import com.varsel.firechat.data.mapper.toUser
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.data.remote.dto.UserDto
import com.varsel.firechat.domain.model.User
import com.varsel.firechat.domain.repository.OtherUserRepository
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OtherUserRepositoryImpl(
    val firebase: Firebase
): OtherUserRepository {
    override suspend fun getUserById(id: String): User = suspendCoroutine { continuation ->
        firebase.getUserById(id, {}, {
            continuation.resume(it.toUser())
        }, {})
    }

    override suspend fun getUserSingle(id: String): User = suspendCoroutine { continuation ->
        firebase.getUserSingle(id, {
            continuation.resume(it.toUser())
        }, {})
    }

    override suspend fun queryUsers(queryString: String): List<User> = suspendCoroutine { continuation ->
        firebase.queryUsers(queryString, {
            continuation.resume(it.map { it.toUser() })
        })
    }

    override suspend fun sendFriendRequest(user: UserEntity): Response = suspendCoroutine { continuation ->
        firebase.sendFriendRequest(user, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun revokeFriendRequest(user: UserEntity): Response = suspendCoroutine { continuation ->
        firebase.revokeFriendRequest(user, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun acceptFriendRequest(user: UserEntity): Response = suspendCoroutine { continuation ->
        firebase.acceptFriendRequest(user, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun rejectFriendRequest(user: UserEntity): Response = suspendCoroutine { continuation ->
        firebase.rejectFriendRequest(user, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun unfriendUser(user: UserEntity): Response = suspendCoroutine { continuation ->
        firebase.unfriendUser(user, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

}