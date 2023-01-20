package com.varsel.firechat.domain.use_case._util.user

import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User

class SearchListOfUsersAndGroups_UseCase {
    operator fun invoke(keyword: String, list: List<Any>): ArrayList<Any> {
        val matches = arrayListOf<Any>()
        if(list.count() < 1){
            return arrayListOf()
        }
        list.map {
            if(it is User){
                val lowerCase = it.name.toLowerCase()
                if(lowerCase.contains(keyword.toLowerCase())){
                    matches.add(it)
                }
            } else if(it is GroupRoom){
                val lowerCase = it.groupName?.toLowerCase()
                if(lowerCase?.contains(keyword.toLowerCase()) == true){
                    matches.add(it)
                }
            }

        }

        return matches
    }
}