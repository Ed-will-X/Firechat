package com.varsel.firechat.domain.use_case.other_user

import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.data.repository.OtherUserRepositoryImpl
import kotlinx.coroutines.flow.Flow

class GetOtherUserSingle(
    private val repository: OtherUserRepositoryImpl
) {
    operator fun invoke(id: String) : Flow<User> {
        return repository.getUserById(id)
    }
}