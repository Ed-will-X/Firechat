package com.varsel.firechat.model.User

import android.widget.ImageView
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.varsel.firechat.model.Chat.ChatRoom

@Entity(tableName = "user")
open class User{
//    var userId: Long? = 0L

    @PrimaryKey
    var userUID: String? = null

    var email: String? = null

    var name: String? = null

    var about: String? = null

    var phone: String? = null

    @ColumnInfo(name = "profile_image")
//    var profileImage: ImageView? = null
    var profileImage: Array<Byte>? = null

    @ColumnInfo(name = "profile_lock")
    var profileLock: Boolean = false

    @ColumnInfo(name = "chat_rooms")
    var chatRooms: HashMap<String, String>? = null

    @ColumnInfo(name = "group_rooms")
    var groupRooms: List<String>? = null

    var occupation: String? = null

    var favoriteChats: List<String>? = null

    var favoriteGroups: List<String>? = null

    var friends: HashMap<String, String>? = null

    var posts: HashMap<String, String>? = null

    @ColumnInfo(name = "friend_requests")
    var friendRequests: HashMap<String, String>? = null

    // Firebase Constructor
    constructor(name: String,
                email: String,
                userUID: String,
                about: String?,
                phone: String?,
                profileImage: Array<Byte>?,
                profileLock: Boolean,
                chatRooms: HashMap<String, String>?,
                groupRooms: List<String>?,
                occupation: String?,
                friendRequests: HashMap<String, String>?
    ){
        this.name = name
        this.email = email
        this.about = about
        this.userUID = userUID
        this.phone = phone
        this.profileImage = profileImage
        this.profileLock = profileLock
        this.chatRooms = chatRooms
        this.groupRooms = groupRooms
        this.occupation = occupation
        this.friendRequests = friendRequests
    }

    constructor(){

    }

}