package com.varsel.firechat.data.repository

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.OtherUserRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OtherUserRepositoryImpl @Inject constructor(
    val firebase: Firebase,
    val databaseReference: DatabaseReference
): OtherUserRepository {
    override fun getUserById(id: String): Flow<User> = callbackFlow {
        firebase.getUserById(id, {}, {
            trySend(it)

        }, {},{ cancel() })
    }

    override fun getUserRecurrent(id: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firebase.getUserRecurrent(id, {
            trySendBlocking(Resource.Success(it))
        }, {  },{  })

        awaitClose {
            Log.d("CLEAN", "Await close ran for get user recurrent")
            databaseReference.removeEventListener(listener)
        }
    }

    override fun queryUsers(queryString: String): Flow<List<User>> = callbackFlow {
        firebase.queryUsers(queryString, {
            trySend(it)
        }, { })

        awaitClose {  }
    }

    override fun sendFriendRequest(user: User): Flow<Response> = callbackFlow {
        firebase.sendFriendRequest(user, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun revokeFriendRequest(user: User): Flow<Response> = callbackFlow {
        firebase.revokeFriendRequest(user, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })
        awaitClose {  }
    }

    override fun acceptFriendRequest(user: User): Flow<Response> = callbackFlow {
        firebase.acceptFriendRequest(user, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun rejectFriendRequest(user: User): Flow<Response> = callbackFlow {
        firebase.rejectFriendRequest(user, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun unfriendUser(user: User): Flow<Response> = callbackFlow {
        firebase.unfriendUser(user, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

}