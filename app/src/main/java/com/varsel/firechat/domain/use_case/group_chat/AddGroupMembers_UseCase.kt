package com.varsel.firechat.domain.use_case.group_chat

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.domain.repository.FirebaseRepository
import com.varsel.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AddGroupMembers_UseCase @Inject constructor(
    val firebaseRepository: FirebaseRepository
) {
    operator fun invoke(users: List<String>, groupID: String): Flow<Response> = callbackFlow {
        firebaseRepository.getFirebaseInstance().addGroupMembers(users, groupID, {

        }, {

        }, {
            trySend(Response.Success())
        })

        awaitClose {  }
    }
}