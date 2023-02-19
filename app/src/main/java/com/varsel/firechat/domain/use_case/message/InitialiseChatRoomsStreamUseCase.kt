package com.varsel.firechat.domain.use_case.message

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.repository.MessageRepositoryImpl
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InitialiseChatRoomsStreamUseCase @Inject constructor(
    val repository: MessageRepository
) {
    operator fun invoke(scope: CoroutineScope): Flow<Response> {
        return repository.initialiseGetChatRoomsRecurrentStream(scope)
    }
}