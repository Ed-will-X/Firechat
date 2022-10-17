package com.varsel.firechat.model.Image

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "image_table")
class Image {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    lateinit var imageId: String

    @ColumnInfo(name = "owner_id")
    lateinit var ownerId: String

    var imgChangeTimestamp: Long = 0L

    var image: String? = null

    var type: Int = 0

    constructor(imageId: String, ownerId: String, image: String, type: Int, imgChangeTimestamp: Long){
        this.imageId = imageId
        this.ownerId = ownerId
        this.image = image
        this.type = type
        this.imgChangeTimestamp = imgChangeTimestamp
    }

    constructor()
}

class ImageType {
    companion object {
        val PROFILE_IMAGE = 0
        val CHAT_IMAGE = 1
    }
}