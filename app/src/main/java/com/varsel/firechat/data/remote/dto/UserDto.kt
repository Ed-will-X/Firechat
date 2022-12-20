package com.varsel.firechat.data.remote.dto

// TODO: Replace all nulls with empty objects
data class UserDto(
    val userUID: String,
    val email: String,
    val name: String,
    val imgChangeTimestamp: Long = 0L,
    val about: String? = null,
    val phone: String? = null,
    val profileLock: Boolean = false,
    val chatRooms: HashMap<String, Long>? = null,
    val groupRooms: HashMap<String, Long> = hashMapOf(),
    val occupation: String? = null,
    val favoriteChats: List<String>? = null,
    val favoriteGroups: HashMap<String, Long>? = null,
    val friends: HashMap<String, Long> = hashMapOf(),
    val gender: String? = null,
    val location: String? = null,
    val friendRequests: HashMap<String, Long> = hashMapOf(),
    val favorite_groups: HashMap<String, String>? = null,
    val favorite_chats: HashMap<String, String>? = null,
    val favorite_friends: HashMap<String, String>? = null,
    val public_posts: HashMap<String, Long>? = null,
    val recent_search: HashMap<String, Long> = hashMapOf()
)