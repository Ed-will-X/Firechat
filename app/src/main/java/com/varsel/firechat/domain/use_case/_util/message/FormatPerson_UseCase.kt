package com.varsel.firechat.domain.use_case._util.message

import com.varsel.firechat.presentation.signedIn.SignedinActivity

class FormatPerson_UseCase {
    operator fun invoke(userId: String, activity: SignedinActivity, secondPersonCallback: ()-> Unit, thirdPersonCallback: ()-> Unit){
        if(userId == activity.firebaseAuth.currentUser!!.uid){
            secondPersonCallback()
        } else {
            thirdPersonCallback()
        }
    }
}