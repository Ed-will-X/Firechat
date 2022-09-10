package com.varsel.firechat.utils

import android.util.Log
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

        // TODO: Fix bug
        fun calculateTimestampDifferenceLess(currentMessageTime: Long, previousMessageTime: Long): Boolean{
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

        fun calculateTimestampDifferenceMore(currentMessageTime: Long, previousMessageTime: Long, timeDifference: Long): Boolean{
            val day: Long = 86400000
            if((currentMessageTime - previousMessageTime) < day){
                return true
            } else {
                return false
            }
        }
    }
}