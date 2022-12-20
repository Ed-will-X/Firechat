package com.varsel.firechat.domain.model

data class ProfileImage(
    val ownerId: String,
    var imgChangeTimestamp: Long = 0L,
    var image: String?
)
