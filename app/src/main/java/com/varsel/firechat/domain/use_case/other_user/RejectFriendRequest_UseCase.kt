package com.varsel.firechat.domain.use_case.other_user

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.OtherUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RejectFriendRequest_UseCase @Inject constructor(
    val otherUserRepository: OtherUserRepository
) {
    operator fun invoke(user: User): Flow<Response> {
        return otherUserRepository.rejectFriendRequest(user)
    }
}