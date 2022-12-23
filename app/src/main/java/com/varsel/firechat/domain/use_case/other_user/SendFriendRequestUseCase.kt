package com.varsel.firechat.domain.use_case.other_user

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.OtherUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendFriendRequestUseCase @Inject constructor(
    val repository: OtherUserRepository
) {
    operator fun invoke(user: User): Flow<Response> {
        val response = repository.sendFriendRequest(user)
        return response
    }
}