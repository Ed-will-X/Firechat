package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage

interface ProfileImageRepository {
    suspend fun uploadProfileImage(profileImage: ProfileImage, base64: String) : Response
    suspend fun removeProfileImage(userId: String) : Response

    suspend fun uploadGroupImage(groupRoom: GroupRoom, profileImage: ProfileImage, base64: String) : Response
    suspend fun removeGroupImage(groupRoom: GroupRoom) : Response

    suspend fun getProfileImage(userId: String) : ProfileImage?

}