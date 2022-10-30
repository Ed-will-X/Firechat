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

    constructor(imageId: String, ownerId: String, image: String){
        this.imageId = imageId
        this.ownerId = ownerId
        this.image = image
    }

    constructor()
}
