package com.varsel.firechat.common._utils

import android.util.Log
import com.varsel.firechat.data.local.User.User
import kotlin.system.measureTimeMillis

class DebugUtils {
    companion object {
        fun log_array(array: List<Any>, tag: String){
            for(i in array){
                Log.d(tag, "${i}")
            }
        }

        fun log_array_users(array: List<User>){
            for(i in array){
                Log.d("LLL", "${i.name}")
            }
        }

        fun log_firebase(string: String){
            Log.d("FIREBASE_VM", string)
        }

        fun measure(text: String, code: () -> Unit) {
            val time = measureTimeMillis(code)
        }
    }
}