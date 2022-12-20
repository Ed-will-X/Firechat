package com.varsel.firechat.data.mapper

import com.varsel.firechat.data.remote.dto.UserDto
import com.varsel.firechat.domain.model.User

fun UserDto.toUser(): User {
    return User (
        userUID,
        email,
        name,
        imgChangeTimestamp,
        about,
        phone,
        chatRooms,
        groupRooms,
        occupation,
        favoriteChats,
        favoriteGroups
    )
}