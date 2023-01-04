package com.varsel.firechat.domain.use_case.profile_image

import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.repository.ProfileImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RemoveProfileImageUseCase @Inject constructor(
    val repository: ProfileImageRepository
) {
    suspend operator fun invoke(): Flow<Response> {
        return repository.removeProfileImage()
    }
}