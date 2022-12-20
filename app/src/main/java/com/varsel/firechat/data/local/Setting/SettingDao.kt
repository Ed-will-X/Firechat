package com.varsel.firechat.data.local.Setting

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: SettingEntity)

    @Update
    suspend fun update(setting: SettingEntity)

    @Delete
    suspend fun delete(setting: SettingEntity)

    @Query("SELECT * FROM setting_table WHERE userId = :userId")
    fun get(userId: String): LiveData<SettingEntity>
}