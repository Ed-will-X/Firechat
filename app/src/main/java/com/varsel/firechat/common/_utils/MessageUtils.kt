package com.varsel.firechat.common._utils

import java.util.*

class MessageUtils {
    companion object{
        // TODO: Remove length
        fun generateUID() : String {
            return "${System.currentTimeMillis()}-${UUID.randomUUID()}"
        }
    }
}