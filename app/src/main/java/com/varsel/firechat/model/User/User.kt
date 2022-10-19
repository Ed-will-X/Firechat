package com.varsel.firechat.model.User

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
open class User{
//    var userId: Long? = 0L

    // change userUID to non-nullable
    @PrimaryKey
    lateinit var userUID: String

    var email: String? = null

    lateinit var name: String

    var imgChangeTimestamp: Long = 0L

    var about: String? = null

    var phone: String? = null

//    @ColumnInfo(name = "profile_image")
//    var profileImage: Array<Byte>? = null

//    val profileImage: String? = null

    @ColumnInfo(name = "profile_lock")
    var profileLock: Boolean = false

    @ColumnInfo(name = "chat_rooms")
    var chatRooms: HashMap<String, String>? = null

    @ColumnInfo(name = "group_rooms")
    var groupRooms: HashMap<String, String>? = null

    var occupation: String? = null

    var favoriteChats: List<String>? = null

    var favoriteGroups: HashMap<String, String>? = null

    var friends: HashMap<String, String>? = null

    var posts: HashMap<String, String>? = null

    var gender: String? = null

    var location: String? = null

    @ColumnInfo(name = "friend_requests")
    var friendRequests: HashMap<String, String>? = null

    var favorite_groups: HashMap<String, String>? = null

    var favorite_chats: HashMap<String, String>? = null

    var favorite_friends: HashMap<String, String>? = null

    // Firebase Constructor
    constructor(name: String,
                email: String,
                userUID: String,
                about: String?,
                phone: String?,
                profileLock: Boolean,
                chatRooms: HashMap<String, String>?,
                groupRooms: HashMap<String, String>?,
                occupation: String?,
                friendRequests: HashMap<String, String>?
    ){
        this.name = name
        this.email = email
        this.about = about
        this.userUID = userUID
        this.phone = phone
        this.profileLock = profileLock
        this.chatRooms = chatRooms
        this.groupRooms = groupRooms
        this.occupation = occupation
        this.friendRequests = friendRequests
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