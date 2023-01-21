package com.varsel.firechat.domain.use_case.profile_image

import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.repository.ProfileImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RemoveGroupImage_UseCase @Inject constructor(
    val repository: ProfileImageRepository
) {
    suspend operator fun invoke(roomUID: String): Flow<Response> {
        return repository.removeGroupImage(roomUID)
    }
}