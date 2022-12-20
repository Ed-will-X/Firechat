package com.varsel.firechat.data.local.Setting

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SettingEntity::class], version = 1, exportSchema = false)
abstract class SettingDatabase: RoomDatabase() {
    abstract val settingDao: SettingDao

//    companion object {
//        @Volatile
//        private var INSTANCE: SettingDatabase? = null
//
//        fun getInstance(context: Context): SettingDatabase {
//            synchronized(this){
//                var instance = INSTANCE
//                if(instance == null){
//                    instance = Room.databaseBuilder(context.applicationContext, SettingDatabase:: class.java, "settings_database").build()
//                    INSTANCE = instance
//                }
//                return instance
//            }
//        }
//    }
}