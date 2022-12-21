package com.varsel.firechat.data.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.CurrentUserRepository
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CurrentUserRepositoryImpl @Inject constructor(
    val firebase: Firebase
) : CurrentUserRepository {

    override suspend fun getCurrentUserSingle(): User = suspendCoroutine { continuation ->
        firebase.getCurrentUserSingle({
            continuation.resume(it)
        })
    }

    override suspend fun getCurrentUserRecurrent(): User {
        TODO("Not yet implemented")
    }

    override suspend fun signUp(name: String, email: String, password: String): Response = suspendCoroutine { continuation ->
        firebase.signUp(email, password, {
            firebase.saveUser(name, email, firebase.mAuth.currentUser!!.uid, {
                continuation.resume(Response.Success())
            }, {
                continuation.resume(Response.Fail())
            })
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