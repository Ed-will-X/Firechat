package com.varsel.firechat.data.repository

import android.util.Log
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.CurrentUserRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class CurrentUserRepositoryImpl @Inject constructor(
    val firebase: Firebase
) : CurrentUserRepository {
    // TODO: Remove Experimental Code
    private var currentUser: MutableStateFlow<Resource<User>> = MutableStateFlow(Resource.Loading())

    override fun getCurrentUserSingle(): Flow<User> = callbackFlow {
        firebase.getCurrentUserSingle({
            trySend(it)
        }, cancelCallback = { cancel() })
    }

    override fun initialiseGetUserRecurrentStream(): Flow<Response> = callbackFlow {
        trySend(Response.Loading())
        val listener = firebase.getCurrentUserRecurrent({
            if(it != null) {
                Log.d("CLEAN", "STREAM SUCCESSFULLY ASSIGNED TO PROPERTY")
                currentUser.value = Resource.Success(it)
                trySend(Response.Success())
            } else {
                currentUser.value = Resource.Error("")
                trySend(Response.Fail())
            }
        },{},{})

        awaitClose { firebase.mDbRef.removeEventListener(listener) }
    }

    override fun getCurrentUserRecurrent(): MutableStateFlow<Resource<User>> {
        return currentUser
    }

    override fun signUp(name: String, email: String, password: String): Flow<Response> = callbackFlow {
        firebase.signUp(email, password, {
            firebase.saveUser(name, email, firebase.mAuth.currentUser!!.uid, {
                trySend(Response.Success())
            }, {
                trySend(Response.Fail())
            })
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun signIn(email: String, password: String) : Flow<Response> = callbackFlow {
        firebase.signin(email, password, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun editUser(key: String, value: String) : Flow<Response> = callbackFlow {
        firebase.editUser(key, value, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun addToRecentSearch(userId: String): Flow<Response> = callbackFlow {
        firebase.addToRecentSearch(userId, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun deleteRecentSearchHistory() : Flow<Response> = callbackFlow {
        firebase.deleteRecentSearchHistory({
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun signOut(after: ()-> Unit) {
        after()
    }

    override fun addGroupToFavorites(groupId: String): Flow<Response> = callbackFlow {
        firebase.addGroupToFavorites(groupId, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override fun removeGroupFromFavorites(groupId: String): Flow<Response> = callbackFlow {
        firebase.removeGroupFromFavorites(groupId, {
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

}