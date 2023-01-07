package com.varsel.firechat.domain.use_case.profile_image

import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.domain.repository.ProfileImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class  GetCurrentUserProfileImageUseCase @Inject constructor(
    val repository: ProfileImageRepository
) {
    suspend operator fun invoke(): Flow<ProfileImage?> {
        return repository.getCurrentUserImage()
    }
}