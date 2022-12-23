package com.varsel.firechat.domain.use_case.current_user

import android.util.Log
import com.varsel.firechat.common.Resource
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.CurrentUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class GetCurrentUserRecurrentUseCase @Inject constructor(
    val repository: CurrentUserRepository
) {
    operator fun invoke(): MutableStateFlow<Resource<User>> {
        return repository.getCurrentUserRecurrent()
    }
}