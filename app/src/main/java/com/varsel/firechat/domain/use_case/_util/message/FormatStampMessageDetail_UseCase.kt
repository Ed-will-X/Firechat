package com.varsel.firechat.domain.use_case._util.message

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class FormatStampMessageDetail_UseCase @Inject constructor(

) {
    operator fun invoke(epochTimestamp: Long): String {
        val date = Date(epochTimestamp)
        val format = SimpleDateFormat("HH:mm")
        val time = format.format(date)

        return time
    }

}