package com.varsel.firechat.domain.use_case._util.user

import com.varsel.firechat.data.local.User.User

class SortUsersByName_UseCase {
    operator fun invoke(users: List<User>): MutableList<User>{
        val sorted = users.sortedBy {
            it.name
        }.toMutableList()

        return sorted
    }
}