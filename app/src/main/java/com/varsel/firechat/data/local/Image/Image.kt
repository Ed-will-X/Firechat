package com.varsel.firechat.data.local.Image

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_table")
class Image {
    @PrimaryKey(autoGenerate = false)
    lateinit var imageId: String

    @ColumnInfo(name = "owner_id")
    lateinit var ownerId: String

    var image: String? = null

    // Used when adding to the realtime database
    constructor(imageId: String, ownerId: String){
        this.imageId = imageId
        this.ownerId = ownerId
        this.image = null
    }

    // Used when retrieving from the realtime database
    constructor(image: Image, base64: String){
        this.imageId = image.imageId
        this.ownerId = image.ownerId
        this.image = base64
    }

    constructor()
}
