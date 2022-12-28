package com.varsel.firechat.domain.use_case.other_user

import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.OtherUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOtherUserRecurrent @Inject constructor(
    val repository: OtherUserRepository
) {
    operator fun invoke(id: String): Flow<User> {
        return repository.getUserRecurrent(id)
    }
}