package com.varsel.firechat.model.Image

import androidx.lifecycle.LiveData
import androidx.room.*
import com.varsel.firechat.model.ProfileImage.ProfileImage

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: Image)

    @Update
    suspend fun update(image: Image)

    @Delete
    suspend fun delete(image: Image)

    @Query("SELECT * FROM image_table WHERE imageId = :imageId")
    fun get(imageId: String): LiveData<Image>
}