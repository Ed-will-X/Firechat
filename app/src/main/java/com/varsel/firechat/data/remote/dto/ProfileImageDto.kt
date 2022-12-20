package com.varsel.firechat.data.remote.dto

data class ProfileImageDto(
    val ownerId: String,
    var imgChangeTimestamp: Long = 0L,
    var image: String?
)