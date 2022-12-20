package com.varsel.firechat.data.repository.remote

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.data.remote.dto.UserDto
import com.varsel.firechat.domain.repository.remote.CurrentUserRepository
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CurrentUserRepositoryImpl(
    val firebase: Firebase
) : CurrentUserRepository {

    override suspend fun getCurrentUserSingle(): UserDto = suspendCoroutine { continuation ->
        firebase.getCurrentUserSingle({
            continuation.resume(it)
        })
    }

    override suspend fun getCurrentUserRecurrent(): UserDto {
        TODO("Not yet implemented")
    }

    override suspend fun signUp(email: String, password: String): Response = suspendCoroutine { continuation ->
        firebase.signUp(email, password, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun signIn(email: String, password: String) : Response = suspendCoroutine { continuation ->
        firebase.signin(email, password, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }
    override suspend fun saveUser(user: User) : Response = suspendCoroutine { continuation ->
        firebase.saveUser(user.name, user.email, user.userUID, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun editUser(key: String, value: String) : Response = suspendCoroutine { continuation ->
        firebase.editUser(key, value, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun addToRecentSearch(userId: String): Response = suspendCoroutine { continuation ->
        firebase.addToRecentSearch(userId, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun deleteRecentSearchHistory() : Response = suspendCoroutine { continuation ->
        firebase.deleteRecentSearchHistory({
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override fun signOut(after: ()-> Unit) {
        after()
    }

    override suspend fun addGroupToFavorites(groupId: String): Response = suspendCoroutine { continuation ->
        firebase.addGroupToFavorites(groupId, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun removeGroupFromFavorites(groupId: String): Response = suspendCoroutine { continuation ->
        firebase.removeGroupFromFavorites(groupId, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

}