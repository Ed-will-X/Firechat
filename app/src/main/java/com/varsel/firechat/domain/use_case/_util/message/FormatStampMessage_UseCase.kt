package com.varsel.firechat.domain.use_case._util.message

import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class FormatStampMessage_UseCase {
    operator fun invoke(timeString: String): String{
        val prettyTime = PrettyTime(Locale.getDefault())
        val ago: String = prettyTime.format(Date(timeString.toLong()))

        if(ago == "moments ago"){
            return "1s"
        } else if(ago == "moments from now"){
            return "1s"
        }else {
            val arr = ago.split(" ").toTypedArray()

            if(arr[1] == "month") {
                return "${arr[0]} mo"
            }
            return "${arr[0]}${arr[1][0]}"
        }
    }
}