package com.varsel.firechat.utils

import androidx.fragment.app.Fragment
import com.varsel.firechat.R
import com.varsel.firechat.model.User.User

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
    }

    fun getFirstName(name: String): String{
        val arr = name.split(" ").toTypedArray()
        return fragment.getString(R.string.about_user, arr[0])
    }
}