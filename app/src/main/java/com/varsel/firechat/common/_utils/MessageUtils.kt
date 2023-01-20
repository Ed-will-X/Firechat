package com.varsel.firechat.common._utils

import com.varsel.firechat.R
import com.varsel.firechat.common._utils.UserUtils
import com.varsel.firechat.data.local.Chat.ChatRoom
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.SystemMessageType
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class MessageUtils {
    companion object{
        // TODO: Remove length
        fun generateUID() : String {
            return UUID.randomUUID().toString()
        }
    }
}