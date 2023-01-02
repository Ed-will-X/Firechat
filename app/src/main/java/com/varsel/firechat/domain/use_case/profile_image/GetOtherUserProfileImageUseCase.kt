package com.varsel.firechat.domain.use_case.profile_image

import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.repository.ProfileImageRepositoryImpl
import com.varsel.firechat.domain.repository.ProfileImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOtherUserProfileImageUseCase @Inject constructor(
    val profileImageRepository: ProfileImageRepository
) {
    suspend operator fun invoke(user: User): Flow<ProfileImage?> {
        return profileImageRepository.getProfileImage(user)
    }
}