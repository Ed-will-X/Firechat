package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import kotlinx.coroutines.flow.Flow

interface ProfileImageRepository {
    suspend fun uploadProfileImage(profileImage: ProfileImage, base64: String) : Flow<Response>
    suspend fun removeProfileImage() : Flow<Response>

    suspend fun uploadGroupImage(groupRoom: GroupRoom, profileImage: ProfileImage, base64: String) : Flow<Response>
    suspend fun removeGroupImage(groupRoom: GroupRoom) : Flow<Response>

    suspend fun getProfileImage(user: User) : Flow<ProfileImage?>
    suspend fun getGroupImage(groupRoom: GroupRoom) : Flow<ProfileImage?>
    suspend fun getCurrentUserImage(): Flow<ProfileImage?>

}