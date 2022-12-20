package com.varsel.firechat.data.mapper

import com.varsel.firechat.data.remote.dto.ProfileImageDto
import com.varsel.firechat.domain.model.ProfileImage

fun ProfileImageDto.toProfileImage(): ProfileImage {
    return ProfileImage(
        ownerId = ownerId,
        imgChangeTimestamp = imgChangeTimestamp,
        image = image
    )
}