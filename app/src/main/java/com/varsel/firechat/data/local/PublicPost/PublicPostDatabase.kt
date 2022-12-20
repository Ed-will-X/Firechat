package com.varsel.firechat.data.local.PublicPost

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PublicPostEntity::class], version = 1, exportSchema = false)
abstract class PublicPostDatabase: RoomDatabase() {
    abstract val publicPostDao: PublicPostDao

    companion object {
        @Volatile
        private var INSTANCE: PublicPostDatabase? = null

//        fun getInstance(context: Context): PublicPostDatabase {
//            synchronized(this){
//                var instance = INSTANCE
//                if(instance == null){
//                    instance = Room.databaseBuilder(context.applicationContext, PublicPostDatabase:: class.java, "public_post_database").build()
//                    INSTANCE = instance
//                }
//                return instance
//            }
//        }
    }
}