package com.varsel.firechat.domain.use_case._util.user

import com.varsel.firechat.data.local.User.User

class SortUsersByNameInGroup_UseCase {
    operator fun invoke(users: List<User?>, admins: List<String>, currentUser: String): MutableList<User?>{
        // TODO: Remove current user from array before sort
        val sorted = users?.sortedBy {
            it?.name
        }.toMutableList()

        // TODO: Add current user to the end of array after sort

        return sorted
    }
}