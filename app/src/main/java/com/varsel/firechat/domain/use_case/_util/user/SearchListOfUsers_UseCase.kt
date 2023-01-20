package com.varsel.firechat.domain.use_case._util.user

import com.varsel.firechat.data.local.User.User

class SearchListOfUsers_UseCase {
    operator fun invoke(keyword: String, list: List<User>): ArrayList<User> {
        val matches = arrayListOf<User>()
        list.map {
            val lowerCase = it.name.toLowerCase()
            if(lowerCase.contains(keyword.toLowerCase())){
                matches.add(it)
            }
        }

        return matches
    }
}