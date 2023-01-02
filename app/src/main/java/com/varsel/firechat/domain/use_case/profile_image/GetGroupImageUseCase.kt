package com.varsel.firechat.domain.use_case.profile_image

import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.ProfileImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupImageUseCase @Inject constructor(
    val repository: ProfileImageRepository
) {
    suspend operator fun invoke(group: GroupRoom): Flow<ProfileImage?> {
        return repository.getGroupImage(group)
    }
}