package com.varsel.firechat.data.local.PublicPost

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PublicPost::class], version = 1, exportSchema = false)
abstract class PublicPostDatabase: RoomDatabase() {
    abstract val publicPostDao: PublicPostDao

    companion object {
        @Volatile
        private var INSTANCE: PublicPostDatabase? = null

    }
}