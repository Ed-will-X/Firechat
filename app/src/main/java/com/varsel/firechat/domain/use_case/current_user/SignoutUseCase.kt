package com.varsel.firechat.domain.use_case.current_user

import com.varsel.firechat.domain.repository.CurrentUserRepository
import javax.inject.Inject

class SignoutUseCase @Inject constructor(
    val repository: CurrentUserRepository
) {
    operator fun invoke() {
        repository.signOut()
    }
}