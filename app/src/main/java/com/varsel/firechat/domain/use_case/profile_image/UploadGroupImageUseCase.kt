package com.varsel.firechat.domain.use_case.profile_image

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.domain.repository.ProfileImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadGroupImageUseCase @Inject constructor(
    val repository: ProfileImageRepository
) {
    suspend operator fun invoke(groupRoom: GroupRoom, profileImage: ProfileImage, base64: String): Flow<Response> {
        if(profileImage.image != null){
            val image_without_base64 = ProfileImage(profileImage.ownerId, profileImage.imgChangeTimestamp)
            return repository.uploadGroupImage(groupRoom, image_without_base64, base64)
        }
        return repository.uploadGroupImage(groupRoom, profileImage, base64)
    }
}