package com.varsel.firechat.utils

import android.util.Log
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class MessageUtils {
    // TODO: Change the >1m text to "moments ago"
    companion object{
        fun formatStampMessage(timeString: String): String{
            val prettyTime = PrettyTime(Locale.getDefault())
            val ago: String = prettyTime.format(Date(timeString.toLong()))
            Log.d("LLL","${ago}")

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
    }
}