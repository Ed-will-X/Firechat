package com.varsel.firechat.domain.use_case.current_user

import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.repository.CurrentUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EditUserUseCase @Inject constructor(
    val repository: CurrentUserRepository
) {
    operator fun invoke(key: String, value: String): Flow<Response> {
        return repository.editUser(key, value)
    }
}

class EditUserFields {
    companion object {
        const val NAME = "name"
        const val ABOUT = "about"
        const val PHONE = "phone"
        const val OCCUPATION = "occupation"
        const val LOCATION = "location"
    }
}