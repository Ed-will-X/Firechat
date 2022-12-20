package com.varsel.firechat.domain.repository

import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage

interface ProfileImageRepository {
    fun uploadProfileImage(profileImage: ProfileImage, base64: String, success: ()-> Unit, fail: ()-> Unit)
    fun removeProfileImage(userId: String, successCallback: ()-> Unit, failureCallback: ()-> Unit)

    fun getProfileImage(userId: String, loopCallback: (profileImage: ProfileImage?) -> Unit, afterCallback: () -> Unit)
    fun appendProfileImageTimestamp(timestamp: Long, successCallback: () -> Unit, failureCallback: () -> Unit)
    fun appendGroupImageTimestamp(groupId: GroupRoom, timestamp: Long, successCallback: () -> Unit, failureCallback: () -> Unit)

}