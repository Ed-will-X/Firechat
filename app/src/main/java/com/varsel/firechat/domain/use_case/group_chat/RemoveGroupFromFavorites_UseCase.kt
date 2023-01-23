package com.varsel.firechat.domain.use_case.group_chat

import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.repository.CurrentUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RemoveGroupFromFavorites_UseCase @Inject constructor(
    val currentUserRepository: CurrentUserRepository
) {
    operator fun invoke(groupId: String): Flow<Response> {
        return currentUserRepository.removeGroupFromFavorites(groupId)
    }
}