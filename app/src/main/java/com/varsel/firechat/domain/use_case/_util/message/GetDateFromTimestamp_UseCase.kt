package com.varsel.firechat.domain.use_case._util.message

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GetDateFromTimestamp_UseCase @Inject constructor(

) {
    operator fun invoke(s: Long): String {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val netDate = Date(s)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }
}