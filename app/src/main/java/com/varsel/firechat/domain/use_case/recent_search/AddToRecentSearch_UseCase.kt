package com.varsel.firechat.domain.use_case.recent_search

import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.repository.CurrentUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddToRecentSearch_UseCase @Inject constructor(
    val currentUserRepository: CurrentUserRepository
) {
    operator fun invoke(userId: String): Flow<Response> {
        return currentUserRepository.addToRecentSearch(userId)
    }
}