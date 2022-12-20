package com.varsel.firechat.domain.model

import com.varsel.firechat.data.local.PublicPost.PublicPostType

data class PublicPost(
    val ownerId: String,
    val postId: String,
    val type: Int = PublicPostType.IMAGE,
    val caption: String? = null,
    val image: String? = null,
    val postTimestamp: Long = 0L
)
