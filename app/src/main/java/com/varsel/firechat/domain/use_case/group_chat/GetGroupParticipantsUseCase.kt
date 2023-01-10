package com.varsel.firechat.domain.use_case.group_chat

import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.FirebaseRepository
import com.varsel.firechat.domain.repository.MessageRepository
import com.varsel.firechat.domain.repository.OtherUserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class GetGroupParticipantsUseCase @Inject constructor(
    val messageRepository: MessageRepository,
    val firebaseRepository: FirebaseRepository
) {
    operator fun invoke(id: String): Flow<List<User>> = callbackFlow {
        var users = mutableListOf<User>()
        for (i in messageRepository.getGroupRoomsRecurrent().value.data ?: listOf()) {
            if(i.roomUID == id) {
                for (j in i.participants.values.toList()){
                    firebaseRepository.getFirebaseInstance().getUserSingle(j, {
                        users.add(it)
                    }, {
                        trySend(users)
                    })
                }
                break
            }
        }

        awaitClose {  }
    }

}