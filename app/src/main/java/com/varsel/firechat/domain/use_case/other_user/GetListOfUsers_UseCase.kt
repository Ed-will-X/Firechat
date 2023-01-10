package com.varsel.firechat.domain.use_case.other_user

import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.FirebaseRepository
import com.varsel.firechat.domain.repository.OtherUserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class GetListOfUsers_UseCase @Inject constructor(
    val otherUserRepository: OtherUserRepository,
    val firebaseRepository: FirebaseRepository
) {
    operator fun invoke(IDs: List<String>): Flow<List<User>> = callbackFlow {
        var users = mutableListOf<User>()

        for (j in IDs){
            firebaseRepository.getFirebaseInstance().getUserSingle(j, {
                users.add(it)
            }, {
                trySend(users)
            })
        }

        awaitClose {  }
    }
}