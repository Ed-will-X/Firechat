package com.varsel.firechat.data.local.Image

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: ImageEntity)

    @Update
    suspend fun update(image: ImageEntity)

    @Delete
    suspend fun delete(image: ImageEntity)

    @Query("SELECT * FROM image_table WHERE imageId = :imageId")
    fun get(imageId: String): LiveData<ImageEntity>
}