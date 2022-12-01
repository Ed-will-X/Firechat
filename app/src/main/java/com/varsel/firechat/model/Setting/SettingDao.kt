package com.varsel.firechat.model.Setting

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: Setting)

    @Update
    suspend fun update(setting: Setting)

    @Delete
    suspend fun delete(setting: Setting)

    @Query("SELECT * FROM setting_table WHERE userId = :userId")
    fun get(userId: String): LiveData<Setting>
}