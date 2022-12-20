package com.varsel.firechat.data.local.Image

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [ImageEntity::class], version = 1, exportSchema = false)
abstract class ImageDatabase: RoomDatabase() {
    abstract val imageDao: ImageDao

    companion object {
        @Volatile
        private var INSTANCE: ImageDatabase? = null

//        fun getInstance(context: Context): ImageDatabase {
//            synchronized(this){
//                var instance = INSTANCE
//                if(instance == null){
//                    instance = Room.databaseBuilder(context.applicationContext, ImageDatabase:: class.java, "image_database").build()
//                    INSTANCE = instance
//                }
//                return instance
//            }
//        }
    }
}