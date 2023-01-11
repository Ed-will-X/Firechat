package com.varsel.firechat.domain.use_case.current_user

import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.repository.CurrentUserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignIn_UseCase @Inject constructor(
    val repository: CurrentUserRepository
) {
    operator fun invoke(email: String, password: String): Flow<Response> {
        return repository.signIn(email, password)
    }
}