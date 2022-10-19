package com.varsel.firechat.model.Image

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "profile_image_table")
class ProfileImage {
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "owner_id")
    lateinit var ownerId: String

    var imgChangeTimestamp: Long = 0L

    var image: String? = null

//    var type: Int = 0

    constructor(ownerId: String, image: String, imgChangeTimestamp: Long){
        this.ownerId = ownerId
        this.image = image
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