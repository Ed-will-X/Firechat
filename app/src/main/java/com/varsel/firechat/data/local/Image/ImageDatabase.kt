package com.varsel.firechat.data.local.Image

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Image::class], version = 1, exportSchema = false)
abstract class ImageDatabase: RoomDatabase() {
    abstract val imageDao: ImageDao

    companion object {
        @Volatile
        private var INSTANCE: ImageDatabase? = null


    }
}