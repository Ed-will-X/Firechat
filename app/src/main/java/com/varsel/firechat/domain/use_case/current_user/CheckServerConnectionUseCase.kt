package com.varsel.firechat.domain.use_case.current_user

import android.util.Log
import com.varsel.firechat.domain.repository.CurrentUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class CheckServerConnectionUseCase @Inject constructor(
    val currentUserRepository: CurrentUserRepository
) {
    operator fun invoke(): MutableStateFlow<Boolean> {
        return currentUserRepository.checkConnectivity()
    }
}