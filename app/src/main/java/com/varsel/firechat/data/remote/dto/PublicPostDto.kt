package com.varsel.firechat.data.remote.dto

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.varsel.firechat.data.local.PublicPost.PublicPostType

data class PublicPostDto(
    val ownerId: String,
    val postId: String,
    val type: Int = PublicPostType.IMAGE,
    val caption: String? = null,
    val image: String? = null,
    val postTimestamp: Long = 0L
)