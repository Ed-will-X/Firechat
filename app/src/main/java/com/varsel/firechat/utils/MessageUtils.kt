package com.varsel.firechat.utils

import com.varsel.firechat.model.Chat.ChatRoom
import com.varsel.firechat.model.message.Message
import org.ocpsoft.prettytime.PrettyTime
import java.util.*
import kotlin.collections.ArrayList

class MessageUtils {
    // TODO: Change the >1m text to "moments ago"
    companion object{
        fun formatStampMessage(timeString: String): String{
            val prettyTime = PrettyTime(Locale.getDefault())
            val ago: String = prettyTime.format(Date(timeString.toLong()))

            if(ago == "moments ago"){
                return "1s"
            } else {
                val arr = ago.split(" ").toTypedArray()
                return "${arr[0]}${arr[1][0]}"
            }
        }

        fun formatStampChatsPage(timeString: String): String{
            val prettyTime = PrettyTime(Locale.getDefault())
            val ago: String = prettyTime.format(Date(timeString.toLong()))

            if(ago == "moments ago"){
                return "1s ago"
            } else {
                val arr = ago.split(" ").toTypedArray()
                return "${arr[0]}${arr[1][0]} ${arr[2]}"
            }
        }

        fun generateUID(length: Int) : String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }

        fun sortMessages(chatRoom: ChatRoom?): List<Message>?{
            val sorted = chatRoom?.messages?.values?.sortedBy {
                it.time
            }

            return sorted
        }

        fun sortChats(chatRooms: List<ChatRoom>?): MutableList<ChatRoom>?{
            val sorted = chatRooms?.sortedBy {
                val sortedMessages = sortMessages(it)

                sortedMessages?.last()?.time
            }?.reversed()?.toMutableList()

            return sorted
        }
    }
}