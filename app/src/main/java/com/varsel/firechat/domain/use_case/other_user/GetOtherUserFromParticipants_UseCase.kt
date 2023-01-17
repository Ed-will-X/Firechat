package com.varsel.firechat.domain.use_case.other_user

import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import javax.inject.Inject

class GetOtherUserFromParticipants_UseCase @Inject constructor(
    val currentUserRepository: CurrentUserRepository
) {
    operator fun invoke(participants: HashMap<String, String>): String {
        var otherUser = ""
        for (i in participants.values){
            if(i != currentUserRepository.getCurrentUserId()){
                otherUser = i
            }
        }

        return otherUser
    }
}