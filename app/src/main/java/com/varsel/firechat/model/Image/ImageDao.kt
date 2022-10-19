package com.varsel.firechat.model.Image

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

//    @Query("DELETE FROM image_table WHERE imageId = :imageId")
//    fun deleteById(imageId: String): LiveData<Image>

    @Query("SELECT * FROM image_table WHERE owner_id = :ownerId")
    fun get(ownerId: String): LiveData<Image>

    // TODO: Add remove all and remove by id
}