package com.varsel.firechat.domain.use_case._util.user

import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserIdUseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import javax.inject.Inject

class GetOtherUserId_UseCase @Inject constructor(
    val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {

    operator fun invoke(participants: HashMap<String, String>): String {
        var otherUser = ""
        for (i in participants.values){
            if(i != getCurrentUserIdUseCase()){
                otherUser = i
            }
        }

        return otherUser
    }
}