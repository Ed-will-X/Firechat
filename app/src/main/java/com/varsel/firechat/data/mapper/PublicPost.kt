package com.varsel.firechat.data.mapper

import com.varsel.firechat.data.remote.dto.PublicPostDto
import com.varsel.firechat.domain.model.PublicPost

fun PublicPostDto.toPublicPost() : PublicPost {
    return PublicPost(
        ownerId,
        postId,
        type,
        caption,
        image,
        postTimestamp
    )
}
