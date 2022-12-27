package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// TODO: Add after callback for get requests
interface CurrentUserRepository {

    fun getCurrentUserId(): String
    fun getCurrentUserSingle(): Flow<User?>
    fun getCurrentUserRecurrent(): MutableStateFlow<Resource<User>>
    fun initialiseGetUserRecurrentStream(): Flow<Response>

    fun updateFriends(friends: HashMap<String, Long>): Flow<Response>
    fun getFriendsSingle(): MutableStateFlow<Resource<List<User>>>

    fun signUp(name: String, email: String, password: String) : Flow<Response>
    fun signIn(email: String, password: String) : Flow<Response>
    fun editUser(key: String, value: String) : Flow<Response>
    fun addToRecentSearch(userId: String) : Flow<Response>
    fun deleteRecentSearchHistory() : Flow<Response>

    fun signOut(after: ()-> Unit)

    fun addGroupToFavorites(groupId: String) : Flow<Response>
    fun removeGroupFromFavorites(groupId: String) : Flow<Response>
}