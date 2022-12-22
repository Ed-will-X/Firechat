package com.varsel.firechat.domain.use_case.OtherUserUseCase

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.OtherUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UnfriendUserUseCase @Inject constructor(
    val repository: OtherUserRepository
) {
    operator fun invoke(user: User): Flow<Response> {
        return repository.unfriendUser(user)
    }
}