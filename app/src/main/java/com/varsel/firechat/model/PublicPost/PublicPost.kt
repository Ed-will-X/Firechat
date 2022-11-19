package com.varsel.firechat.model.PublicPost

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "public_post_table")
class PublicPost {
    @ColumnInfo(name = "owner_id")
    lateinit var ownerId: String

    @PrimaryKey(autoGenerate = false)
    lateinit var postId: String

    var type: Int = PublicPostType.IMAGE

    var caption: String? = null

    var image: String = ""

    var postTimestamp: Long = 0L

    // TODO: Add video and audio support

    constructor()

    constructor(ownerId: String, postId: String, type: Int, caption: String?, postTimestamp: Long){
        this.ownerId = ownerId
        this.postId = postId
        this.type = type
        this.caption = caption
        this.postTimestamp = postTimestamp
    }

    constructor(publicPost: PublicPost, base64: String){
        this.ownerId = publicPost.ownerId
        this.postId = publicPost.postId
        this.type = publicPost.type
        this.caption = publicPost.caption
        this.postTimestamp = postTimestamp
        this.image = base64
    }
}

class PublicPostType {
    companion object {
        val IMAGE = 0
        val VIDEO = 1
        val AUDIO = 2
    }
}