package com.varsel.firechat.domain.use_case._util.message

class CalculateTimestampDifferenceLess_UseCase {
    operator fun invoke(currentMessageTime: Long, previousMessageTime: Long): Boolean{
        val day: Long = 86400000
        val hour: Long = 3600000
        val timestampNow = System.currentTimeMillis()

        if((currentMessageTime + day) > timestampNow){
            // apply hour rule
            if ((currentMessageTime - previousMessageTime) < hour){
                return true
            } else {
                return false
            }
        } else {
            // apply day rule
            if ((currentMessageTime - previousMessageTime) < day){
                return true
            } else {
                return false
            }
        }
    }
}