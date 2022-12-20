package com.varsel.firechat.data.repository.remote

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.data.remote.dto.UserDto
import com.varsel.firechat.domain.repository.remote.OtherUserRepository
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OtherUserRepositoryImpl(
    val firebase: Firebase
): OtherUserRepository {
    override suspend fun getUserById(id: String): UserDto = suspendCoroutine { continuation ->
        firebase.getUserById(id, {}, {
            continuation.resume(it)
        }, {})
    }

    override suspend fun getUserSingle(id: String): UserDto = suspendCoroutine { continuation ->
        firebase.getUserSingle(id, {
            continuation.resume(it)
        }, {})
    }

    override suspend fun queryUsers(queryString: String): List<UserDto> = suspendCoroutine { continuation ->
        firebase.queryUsers(queryString, {
            continuation.resume(it)
        })
    }

    override suspend fun sendFriendRequest(user: User): Response = suspendCoroutine { continuation ->
        firebase.sendFriendRequest(user, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun revokeFriendRequest(user: User): Response = suspendCoroutine { continuation ->
        firebase.revokeFriendRequest(user, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun acceptFriendRequest(user: User): Response = suspendCoroutine { continuation ->
        firebase.acceptFriendRequest(user, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun rejectFriendRequest(user: User): Response = suspendCoroutine { continuation ->
        firebase.rejectFriendRequest(user, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun unfriendUser(user: User): Response = suspendCoroutine { continuation ->
        firebase.unfriendUser(user, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

}