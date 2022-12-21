package com.varsel.firechat.data.local.ProfileImage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProfileImage::class], version = 1, exportSchema = false)
abstract class ProfileImageDatabase: RoomDatabase() {
    abstract val profileImageDao: ProfileImageDao

    companion object {
        @Volatile
        private var INSTANCE: ProfileImageDatabase? = null

//        fun getInstance(context: Context): ProfileImageDatabase {
//            synchronized(this){
//                var instance = INSTANCE
//                if(instance == null){
//                    instance = Room.databaseBuilder(context.applicationContext, ProfileImageDatabase:: class.java, "profile_image_database").build()
//                    INSTANCE = instance
//                }
//                return instance
//            }
//        }
    }
}