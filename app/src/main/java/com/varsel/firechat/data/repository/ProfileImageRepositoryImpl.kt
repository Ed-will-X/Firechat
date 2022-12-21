package com.varsel.firechat.data.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.ProfileImageRepository
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProfileImageRepositoryImpl(
    val firebase: Firebase
) : ProfileImageRepository {
    override suspend fun uploadProfileImage(profileImage: ProfileImage, base64: String): Response = suspendCoroutine { continuation ->
        firebase.uploadProfileImage(profileImage, base64, {
            firebase.appendProfileImageTimestamp(System.currentTimeMillis(), {
                continuation.resume(Response.Success())
            }, {
                continuation.resume(Response.Fail())
            })
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun uploadGroupImage(groupRoom: GroupRoom, profileImage: ProfileImage, base64: String): Response = suspendCoroutine { continuation ->
        firebase.uploadGroupImage(groupRoom, profileImage, base64, {
            firebase.appendGroupImageTimestamp(groupRoom.roomUID, System.currentTimeMillis(), {
                continuation.resume(Response.Success())
            }, {
                continuation.resume(Response.Fail())
            })
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun removeGroupImage(groupRoom: GroupRoom): Response = suspendCoroutine { continuation ->
        firebase.removeProfileImage(groupRoom.roomUID, {
            firebase.appendGroupImageTimestamp(groupRoom.roomUID, System.currentTimeMillis(), {
                continuation.resume(Response.Success())
            }, {
                continuation.resume(Response.Fail())
            })
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun removeProfileImage(userId: String): Response = suspendCoroutine { continuation ->
        firebase.removeProfileImage(userId, {
            firebase.appendProfileImageTimestamp(System.currentTimeMillis(), {
                continuation.resume(Response.Success())
            }, {
                continuation.resume(Response.Fail())
            })
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun getProfileImage(userId: String): ProfileImage? = suspendCoroutine { continuation ->
        firebase.getProfileImage(userId, {
            continuation.resume(it)
        }, {}, {})
    }
}