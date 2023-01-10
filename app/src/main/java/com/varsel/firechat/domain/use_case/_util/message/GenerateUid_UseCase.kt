package com.varsel.firechat.domain.use_case._util.message

import java.util.*

class GenerateUid_UseCase {
    operator fun invoke() : String {
        return UUID.randomUUID().toString()
    }
}