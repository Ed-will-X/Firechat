package com.varsel.firechat.domain.use_case.other_user

import com.varsel.firechat.common.Resource
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.OtherUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOtherUserSingle @Inject constructor(
    private val repository: OtherUserRepository
) {
    operator fun invoke(id: String) : Flow<Resource<User>> {
        return repository.getUserById(id)
    }
}