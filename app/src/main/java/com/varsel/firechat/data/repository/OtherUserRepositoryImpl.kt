package com.varsel.firechat.data.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.OtherUserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OtherUserRepositoryImpl @Inject constructor(
    val firebase: Firebase
): OtherUserRepository {
    override fun getUserById(id: String): Flow<User> = callbackFlow {
        firebase.getUserById(id, {}, {
            trySend(it)
        }, {})
    }

    override suspend fun getUserSingle(id: String): User = suspendCoroutine { continuation ->
        firebase.getUserSingle(id, {
            continuation.resume(it)
        }, {})
    }

    override fun queryUsers(queryString: String): Flow<List<User>> = callbackFlow {
        firebase.queryUsers(queryString, {
            trySend(it)
        })

        awaitClose {  }
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