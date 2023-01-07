package com.varsel.firechat.data.repository

import android.util.Log
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.common._utils.UserUtils
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.CurrentUserRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class CurrentUserRepositoryImpl @Inject constructor(
    val firebase: Firebase
) : CurrentUserRepository {
    // TODO: Remove Experimental Code
    private var currentUser: MutableStateFlow<Resource<User>> = MutableStateFlow(Resource.Loading())
    private var friends: MutableStateFlow<Resource<List<User>>> = MutableStateFlow(Resource.Loading())
    private var user_single: User? = null
    val isConnectedToFirebase = MutableStateFlow(false)

    init {
        firebase.checkFirebaseConnection {
            isConnectedToFirebase.value = it
        }
    }

    override fun getCurrentUserId(): String {
        return firebase.mAuth.currentUser!!.uid
    }
    override fun getCurrentUserSingle(): Flow<User?> = callbackFlow {
        firebase.getCurrentUserSingle({
            trySend(it)
        }, cancelCallback = { cancel() })
//        return user_single
    }

    override fun initialiseGetUserRecurrentStream(): Flow<Response> = callbackFlow {
        trySend(Response.Loading())
        val listener = firebase.getCurrentUserRecurrent({
            if(it != null) {
                currentUser.value = Resource.Success(it)
//                user_single = it
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

    override fun updateFriends(friendsMap: HashMap<String, Long>): Flow<Response> = callbackFlow {
        trySend(Response.Loading())
        friends.value = Resource.Loading()

        val sortedMap = UserUtils.sortByTimestamp(friendsMap.toSortedMap())
        val users = mutableListOf<User>()

        if(friendsMap.isNotEmpty()) {
            for(i in sortedMap.keys){
                firebase.getUserSingle(i, {
                    users.add(it)
                }, {
                    if(users.isNotEmpty()) {
                        friends.value = Resource.Success(users)
                        trySend(Response.Success())
                    } else {
                        trySend(Response.Fail())
                        friends.value = Resource.Error("")
                    }
                })
            }
        } else {
            friends.value = Resource.Error("")
            trySend(Response.Fail())
        }

        awaitClose {  }
    }

    override fun getFriendsSingle(): MutableStateFlow<Resource<List<User>>> {
        return friends
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

    override fun checkConnectivity(): MutableStateFlow<Boolean> {
        return isConnectedToFirebase
    }

}