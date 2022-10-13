package com.varsel.firechat.model.Image

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ImageDao {
    @Insert
    suspend fun insert(image: Image)

    @Update
    suspend fun update(image: Image)

    @Delete
    suspend fun delete(image: Image)

//    @Query("DELETE FROM image_table WHERE imageId = :imageId")
//    fun deleteById(imageId: String): LiveData<Image>

    @Query("SELECT * FROM image_table WHERE imageId = :imageId")
    fun get(imageId: String): LiveData<Image>
}