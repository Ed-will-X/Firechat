package com.varsel.firechat.domain.use_case.current_user

import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.CurrentUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserSingleUseCase @Inject constructor(
    val currentUserRepository: CurrentUserRepository
) {
    operator fun invoke(): Flow<User?> {
        return currentUserRepository.getCurrentUserSingle()
    }
}