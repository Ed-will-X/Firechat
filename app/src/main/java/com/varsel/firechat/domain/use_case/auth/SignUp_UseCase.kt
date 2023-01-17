package com.varsel.firechat.domain.use_case.auth

import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.repository.CurrentUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignUp_UseCase @Inject constructor(
    val repository: CurrentUserRepository
) {
    operator fun invoke(name: String, email: String, password: String): Flow<Response> {
        return repository.signUp(name, email, password)
    }
}