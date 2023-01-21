package com.varsel.firechat.data.local.Setting

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Setting::class], version = 1, exportSchema = false)
abstract class SettingDatabase: RoomDatabase() {
    abstract val settingDao: SettingDao

}