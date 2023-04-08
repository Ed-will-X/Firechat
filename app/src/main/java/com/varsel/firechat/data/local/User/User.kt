package com.varsel.firechat.data.local.User

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.collections.HashMap

// TODO: Change all var to val

@Entity(tableName = "user")
open class User{
//    var userId: Long? = 0L

    // change userUID to non-nullable
    @PrimaryKey
    lateinit var userUID: String

    lateinit var email: String

    lateinit var name: String

    var imgChangeTimestamp: Long = 0L

    var about: String? = null

    var phone: String? = null

    @ColumnInfo(name = "profile_lock")
    var profileLock: Boolean = false

    @ColumnInfo(name = "chat_rooms")
    var chatRooms: HashMap<String, Long>? = hashMapOf()

    @ColumnInfo(name = "group_rooms")
    var groupRooms: HashMap<String, Long> = hashMapOf()

    var occupation: String? = null

    var favoriteChats: List<String>? = listOf()

    var favoriteGroups: HashMap<String, Long>? = hashMapOf()

    var friends: HashMap<String, Long> = hashMapOf()

    var gender: String? = null

    var location: String? = null

    var lastOnline: Long = 0L

    @ColumnInfo(name = "friend_requests")
    var friendRequests: HashMap<String, Long> = hashMapOf()

    var favorite_groups: HashMap<String, String>? = hashMapOf()

    var favorite_chats: HashMap<String, String>? = hashMapOf()

    var favorite_friends: HashMap<String, String>? = hashMapOf()

    var public_posts: HashMap<String, Long>? = hashMapOf()

    var recent_search: HashMap<String, Long> = hashMapOf()

    // TODO: Shorten
    constructor(name: String,
                email: String,
                userUID: String
    ){
        this.name = name
        this.email = email
        this.userUID = userUID
    }

    constructor(){

    }
}

// TODO: Change to sealed class

class GenderValues(){
    companion object {
        val MALE = 0
        val FEMALE = 1
        val TRANS = 2
        val MIXED = 3
        val OTHER = 4
    }
}