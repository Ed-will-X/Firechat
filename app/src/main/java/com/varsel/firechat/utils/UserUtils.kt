package com.varsel.firechat.utils

import androidx.fragment.app.Fragment
import com.varsel.firechat.R
import com.varsel.firechat.data.local.Chat.GroupRoomEntity
import com.varsel.firechat.data.local.User.UserEntity
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UserUtils(var fragment: Fragment) {
    companion object {
        fun truncate(about: String, length: Int): String{
            if(about.length > length){
                return "${about.subSequence(0, length)}..."
            } else {
                return about
            }
        }

        fun sortUsersByName(users: List<UserEntity?>): MutableList<UserEntity?>{
            val sorted = users?.sortedBy {
                it?.name
            }.toMutableList()

            return sorted
        }

        fun sortUsersByNameInGroup(users: List<UserEntity?>, admins: List<String>, currentUser: String): MutableList<UserEntity?>{
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

        fun searchListOfUsers(keyword: String, list: List<UserEntity>): ArrayList<UserEntity> {
            val matches = arrayListOf<UserEntity>()
            list.map {
                val lowerCase = it.name.toLowerCase()
                if(lowerCase.contains(keyword.toLowerCase())){
                    matches.add(it)
                }
            }

            return matches
        }

        fun searchListOfUsers_andGroups(keyword: String, list: List<Any>): ArrayList<Any> {
            val matches = arrayListOf<Any>()
            if(list.count() < 1){
                return arrayListOf()
            }
            list.map {
                if(it is UserEntity){
                    val lowerCase = it.name.toLowerCase()
                    if(lowerCase.contains(keyword.toLowerCase())){
                        matches.add(it)
                    }
                } else if(it is GroupRoomEntity){
                    val lowerCase = it.groupName?.toLowerCase()
                    if(lowerCase?.contains(keyword.toLowerCase()) == true){
                        matches.add(it)
                    }
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

        fun getUser(userId: String, activity: SignedinActivity, callback: (user: UserEntity)-> Unit){
            activity.firebaseViewModel.getUserSingle(userId, activity.mDbRef, {
                if(it != null){
                    callback(it)
                }
            },{})
        }

        fun getUser(userId: String, activity: SignedinActivity, userCallback: (user: UserEntity)-> Unit, afterCallback: ()-> Unit){
            activity.firebaseViewModel.getUserSingle(userId, activity.mDbRef, {
                if(it != null){
                    userCallback(it)
                }
            },{
                afterCallback()
            })
        }

        /*
        *   Sorts a hashmap by the order of insertion,
        *   The key refers to the ID,
        *   And the value refers to the insertion timestamp.
        * */
        fun sortByTimestamp(positioned: SortedMap<String, Long>): Map<String, Long> {
            val sorted = positioned.toList()
                .sortedBy { (key, value) -> value }
                .toMap()

            return sorted
        }
    }

    fun getFirstName(name: String): String{
        val arr = name.split(" ").toTypedArray()
        return fragment.getString(R.string.about_user, arr[0])
    }
}