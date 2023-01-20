package com.varsel.firechat.common._utils

import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.presentation.signedIn.SignedinActivity

class UserUtils {
    companion object {
        fun getUser(userId: String, activity: SignedinActivity, callback: (user: User)-> Unit){
            activity.firebaseViewModel.getUserSingle(userId, activity.mDbRef, {
                if(it != null){
                    callback(it)
                }
            },{})
        }

        fun truncate(about: String, length: Int): String{
            if(about.length > length){
                return "${about.subSequence(0, length)}..."
            } else {
                return about
            }
        }
    }
}