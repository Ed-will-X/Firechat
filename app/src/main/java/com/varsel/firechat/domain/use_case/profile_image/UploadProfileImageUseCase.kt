package com.varsel.firechat.domain.use_case.profile_image

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.domain.repository.ProfileImageRepository
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

class UploadProfileImageUseCase @Inject constructor(
    val repository: ProfileImageRepository
) {
    suspend operator fun invoke(profileImage: ProfileImage, base64: String): Flow<Response> {
        if(profileImage.image != null){
            val image_without_base64 = ProfileImage(profileImage.ownerId, profileImage.imgChangeTimestamp)
            return repository.uploadProfileImage(image_without_base64, base64)
        }

        return repository.uploadProfileImage(profileImage, base64)
    }
}