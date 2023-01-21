package com.varsel.firechat.data.local.ProfileImage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProfileImage::class], version = 1, exportSchema = false)
abstract class ProfileImageDatabase: RoomDatabase() {
    abstract val profileImageDao: ProfileImageDao

    companion object {
        @Volatile
        private var INSTANCE: ProfileImageDatabase? = null


    }
}