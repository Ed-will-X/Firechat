package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoomEntity
import com.varsel.firechat.data.local.ProfileImage.ProfileImageEntity
import com.varsel.firechat.domain.model.ProfileImage

interface ProfileImageRepository {
    suspend fun uploadProfileImage(profileImage: ProfileImage, base64: String) : Response
    suspend fun removeProfileImage(userId: String) : Response

    suspend fun uploadGroupImage(groupRoom: GroupRoomEntity, profileImage: ProfileImage, base64: String) : Response
    suspend fun removeGroupImage(groupRoom: GroupRoomEntity) : Response

    suspend fun getProfileImage(userId: String) : ProfileImage?

}