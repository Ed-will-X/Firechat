package com.varsel.firechat.data.local.Image

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: Image)

    @Update
    suspend fun update(image: Image)

    @Delete
    suspend fun delete(image: Image)

    @Query("SELECT * FROM image_table WHERE imageId = :imageId")
    fun get_liveData(imageId: String): LiveData<Image>

    @Query("SELECT * FROM image_table WHERE imageId = :imageId")
    suspend fun get(imageId: String): Image?
}