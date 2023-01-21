package com.varsel.firechat.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.ProfileImage.ProfileImageDao
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.domain.repository.ProfileImageRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.coroutines.resume

class ProfileImageRepositoryImpl @Inject constructor(
    val firebase: Firebase,
    val dao: ProfileImageDao,
    val currentUserRepository: CurrentUserRepository
) : ProfileImageRepository {
    val profileImageFetchBlacklist = MutableLiveData<HashMap<String, Long>>(hashMapOf())

    override suspend fun uploadProfileImage(profileImage: ProfileImage, base64: String): Flow<Response> = callbackFlow {
        trySend(Response.Loading())

        firebase.uploadProfileImage(profileImage, base64, {
            firebase.appendProfileImageTimestamp(profileImage.imgChangeTimestamp, {
                trySend(Response.Success())
                val profileImage_withBase64 = ProfileImage(profileImage, base64)
                runBlocking { dao.insert(profileImage_withBase64) }
            }, {
                trySend(Response.Fail())
            })
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override suspend fun uploadGroupImage(roomId: String, profileImage: ProfileImage, base64: String): Flow<Response> = callbackFlow {
        trySend(Response.Loading())

        firebase.uploadGroupImage(roomId, profileImage, base64, {
            firebase.appendGroupImageTimestamp(roomId, profileImage.imgChangeTimestamp, {
                trySend(Response.Success())

                val profileImage_withBase64 = ProfileImage(profileImage, base64)
                runBlocking { dao.insert(profileImage_withBase64) }
            }, {
                trySend(Response.Fail())
            })
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override suspend fun removeGroupImage(roomUID: String): Flow<Response> = callbackFlow {
        trySend(Response.Loading())

        firebase.removeProfileImage(roomUID, {
            firebase.appendGroupImageTimestamp(roomUID, System.currentTimeMillis(), {
                trySend(Response.Success())
                runBlocking { nullifyImageInRoom(roomUID) }
            }, {
                trySend(Response.Fail())
            })
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override suspend fun removeProfileImage(): Flow<Response> = callbackFlow {
        trySend(Response.Loading())

        firebase.removeProfileImage(firebase.mAuth.currentUser!!.uid, {
            firebase.appendProfileImageTimestamp(System.currentTimeMillis(), {
                trySend(Response.Success())
                runBlocking { nullifyImageInRoom(firebase.mAuth.currentUser!!.uid) }
            }, {
                trySend(Response.Fail())
            })
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override suspend fun getProfileImage(user: User): Flow<ProfileImage?> = callbackFlow {
        val image = dao.get(user.userUID)
        if(image != null && user.imgChangeTimestamp == image.imgChangeTimestamp) {
            // Image hasn't changed in database
            trySend(image)
        } else if(image != null && image.imgChangeTimestamp == 0L){

        } else {
            // image is null or has changed, so re-fetch
            isNotUserInBlacklist(user,{
                addUserToBlacklist(user)

                firebase.getProfileImage(user.userUID, {
                    trySend(it)
                    // Save in local db when fetched
                    runBlocking { dao.insert(it) }
                }, {
                    trySend(null)
                }, { exists ->
                    // Callback runs when image is null to minimise data usage
                    if(!exists) {
                        runBlocking { nullifyImageInRoom(user.userUID) }
                    }
                })
            }, {
                trySend(null)
            })
        }

        awaitClose {  }
    }

    override suspend fun getGroupImage(groupRoom: GroupRoom): Flow<ProfileImage?> = callbackFlow {
        val image = dao.get(groupRoom.roomUID)
        if(image != null && groupRoom.imgChangeTimestamp == image.imgChangeTimestamp) {
            // Image hasn't changed in database
            trySend(image)
        } else if(image != null && image.imgChangeTimestamp == 0L){

        } else {
            // image is null or has changed, so re-fetch
            isNotGroupInBlacklist(groupRoom,{
                addGroupToBlacklist(groupRoom)

                firebase.getProfileImage(groupRoom.roomUID, {
                    trySend(it)
                    // Save in local db when fetched
                    runBlocking { dao.insert(it) }
                }, {
                    trySend(null)
                }, { exists ->
                    // Callback runs when image is null to minimise data usage
                    if(!exists) {
                        runBlocking { nullifyImageInRoom(groupRoom.roomUID) }
                    }
                })
            }, {
                trySend(null)
            })
        }

        awaitClose {  }
    }

    override suspend fun getCurrentUserImage(): Flow<ProfileImage?> = callbackFlow {
        val image = dao.get(firebase.mAuth.currentUser!!.uid)
        val user = currentUserRepository.getCurrentUserRecurrent().value.data

        if(image != null && user?.imgChangeTimestamp == image.imgChangeTimestamp){
            trySend(image)
        } else if(image != null && image.imgChangeTimestamp == 0L){

        } else {
            firebase.getProfileImage(firebase.mAuth.currentUser!!.uid, {
                runBlocking { dao.insert(it) }
                trySend(it)
            }, {}, { exists ->
                if(!exists) {
                    runBlocking { nullifyImageInRoom(firebase.mAuth.currentUser!!.uid) }
                }
            })
        }

        awaitClose {  }
    }

    suspend fun nullifyImageInRoom(id: String){
        val profileImage = ProfileImage(id, 0L, null)
        dao.insert(profileImage)
    }

    fun isNotUserInBlacklist(user: User, ifCallback: ()-> Unit, elseCallback: ()-> Unit){
        if(profileImageFetchBlacklist.value?.contains(user.userUID) == false){
            ifCallback()
        } else {
            val userimgTimestampBlacklist = profileImageFetchBlacklist.value?.get(user.userUID)

            if(userimgTimestampBlacklist != user.imgChangeTimestamp){
                ifCallback()
            } else {
                elseCallback()
            }
        }
    }

    fun addUserToBlacklist(user: User){
        profileImageFetchBlacklist.value?.put(user.userUID, user.imgChangeTimestamp)
    }

    fun isNotGroupInBlacklist(group: GroupRoom, ifCallback: ()-> Unit, elseCallback: ()-> Unit){
        if(profileImageFetchBlacklist.value?.contains(group.roomUID) == false){
            ifCallback()
        } else {
            val userimgTimestampBlacklist = profileImageFetchBlacklist.value?.get(group.roomUID)

            if(userimgTimestampBlacklist != group.imgChangeTimestamp){
                ifCallback()
            } else {
                elseCallback()
            }
        }
    }

    fun addGroupToBlacklist(group: GroupRoom){
        profileImageFetchBlacklist.value?.put(group.roomUID, group.imgChangeTimestamp)
    }

    fun clearBlacklist(){
        profileImageFetchBlacklist.value?.clear()
    }
}