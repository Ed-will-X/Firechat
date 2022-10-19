package com.varsel.firechat.model.ProfileImage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_image_table")
class ProfileImage {
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "owner_id")
    lateinit var ownerId: String

    var imgChangeTimestamp: Long = 0L

    var image: String? = null

    constructor(ownerId: String, image: String, imgChangeTimestamp: Long){
        this.ownerId = ownerId
        this.image = image
        this.imgChangeTimestamp = imgChangeTimestamp
    }

    constructor()
}