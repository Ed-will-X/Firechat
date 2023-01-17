package com.varsel.firechat.domain.use_case.other_user

import com.varsel.firechat.common.Resource
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.repository.OtherUserRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOtherUserSingle @Inject constructor(
    private val repository: OtherUserRepositoryImpl
) {
    operator fun invoke(id: String) : Flow<Resource<User>> {
        return repository.getUserById(id)
    }
}