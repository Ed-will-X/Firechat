package com.varsel.firechat.model.User

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

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
    var chatRooms: HashMap<String, Long>? = null

    @ColumnInfo(name = "group_rooms")
    var groupRooms: HashMap<String, Long> = hashMapOf()

    var occupation: String? = null

    var favoriteChats: List<String>? = null

    var favoriteGroups: HashMap<String, Long>? = null

    var friends: HashMap<String, Long> = hashMapOf()

    var gender: String? = null

    var location: String? = null

    @ColumnInfo(name = "friend_requests")
    var friendRequests: HashMap<String, Long> = hashMapOf()

    var favorite_groups: HashMap<String, String>? = null

    var favorite_chats: HashMap<String, String>? = null

    var favorite_friends: HashMap<String, String>? = null

    var public_posts: HashMap<String, Long>? = null

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

class GenderValues(){
    companion object {
        val MALE = 0
        val FEMALE = 1
        val TRANS = 2
        val MIXED = 3
        val OTHER = 4
    }
}