package com.varsel.firechat.utils

import androidx.fragment.app.Fragment
import com.varsel.firechat.R
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity

class UserUtils(var fragment: Fragment) {
    companion object {
        fun truncate(about: String, length: Int): String{
            if(about.length > length){
                return "${about.subSequence(0, length)}..."
            } else {
                return about
            }
        }

        fun sortUsersByName(users: List<User?>): MutableList<User?>{
            val sorted = users?.sortedBy {
                it?.name
            }.toMutableList()

            return sorted
        }

        fun sortUsersByNameInGroup(users: List<User?>, admins: List<String>, currentUser: String): MutableList<User?>{
            // TODO: Remove current user from array before sort
            val sorted = users?.sortedBy {
                it?.name
            }.toMutableList()

            // TODO: Add current user to the end of array after sort

            return sorted
        }

        fun searchList(keyword: String, list: List<String>): List<String> {
            val matches = mutableListOf<String>()
            list.map {
                val lowerCase = it.toLowerCase()
                if(lowerCase.contains(keyword.toLowerCase())){
                    matches.add(it)
                }
            }

            return matches
        }

        fun searchListOfUsers(keyword: String, list: List<User>): ArrayList<User> {
            val matches = arrayListOf<User>()
            list.map {
                val lowerCase = it.name.toLowerCase()
                if(lowerCase.contains(keyword.toLowerCase())){
                    matches.add(it)
                }
            }

            return matches
        }

        fun getOtherUserId(participants: HashMap<String, String>, activity: SignedinActivity): String{
            var otherUser = ""
            for (i in participants.values){
                if(i != activity.firebaseAuth.currentUser?.uid.toString()){
                    otherUser = i
                }
            }

            return otherUser
        }
    }

    fun getFirstName(name: String): String{
        val arr = name.split(" ").toTypedArray()
        return fragment.getString(R.string.about_user, arr[0])
    }
}