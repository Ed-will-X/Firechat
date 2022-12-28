package com.varsel.firechat.domain.use_case.message

import com.varsel.firechat.common.Response
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InitialiseGroupRoomsStreamUseCase @Inject constructor(
    val repository: MessageRepository
) {
    operator fun invoke(): Flow<Response> {
        return repository.initialiseGetGroupRoomsRecurrentStream()
    }
}