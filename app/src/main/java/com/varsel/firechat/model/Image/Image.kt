package com.varsel.firechat.model.Image

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_table")
class Image {
    @PrimaryKey(autoGenerate = false)
    lateinit var imageId: String

    @ColumnInfo(name = "owner_id")
    lateinit var ownerId: String

    lateinit var image: String

//    constructor(imageId: String, ownerId: String, image: String){
//        this.imageId = imageId
//        this.ownerId = ownerId
//        this.image = image
//    }

    // Used when adding to the realtime database
    constructor(imageId: String, ownerId: String){
        this.imageId = imageId
        this.ownerId = ownerId
        this.image = ""
    }

    // Used when retrieving from the realtime database
    constructor(image: Image, base64: String){
        this.imageId = image.imageId
        this.ownerId = image.ownerId
        this.image = base64
    }

    constructor()
}
