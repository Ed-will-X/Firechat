package com.varsel.firechat.domain.use_case._util.string

class Truncate_UseCase {
    operator fun invoke(about: String, length: Int): String{
        if(about.length > length){
            return "${about.subSequence(0, length)}..."
        } else {
            return about
        }
    }
}