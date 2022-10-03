package com.varsel.firechat.utils

import android.util.Log
import com.varsel.firechat.model.User.User

class DebugUtils {
    companion object {
        fun log_array(array: List<Any>, tag: String){
            for(i in array){
                Log.d(tag, "${i}")
            }
        }

        fun log_array_users(array: List<User>, tag: String){
            for(i in array){
                Log.d(tag, "${i.name}")
            }
        }
    }
}