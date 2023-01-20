package com.varsel.firechat.domain.use_case._util.message

import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class FormatTimestampChatsPage_UseCase {
    // TODO: Use string resources
    operator fun invoke(timeString: String): String{
        val prettyTime = PrettyTime(Locale.getDefault())
        val ago: String = prettyTime.format(Date(timeString.toLong()))

        if(ago == "moments ago"){
            return "1s ago"
        } else if(ago == "moments from now"){
            return "1s ago"
        } else {
            val arr = ago.split(" ").toTypedArray()
            return "${arr[0]}${arr[1][0]} ${arr[2]}"
        }
    }
}