package com.varsel.firechat.data.local.ProfileImage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_image_table")
class ProfileImageEntity {
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "owner_id")
    lateinit var ownerId: String

    var imgChangeTimestamp: Long = 0L

    var image: String? = null

    constructor(ownerId: String, imgChangeTimestamp: Long){
        this.ownerId = ownerId
        this.imgChangeTimestamp = imgChangeTimestamp
    }

    constructor(profileImage: ProfileImageEntity, base64: String){
        this.ownerId = profileImage.ownerId
        this.imgChangeTimestamp = profileImage.imgChangeTimestamp
        this.image = base64
    }

    // this constructor is solely to nullify image in room
    constructor(ownerId: String, imgChangeTimestamp: Long, base64: String?){
        this.ownerId = ownerId
        this.imgChangeTimestamp = imgChangeTimestamp
        this.image = base64
    }

    constructor()
}