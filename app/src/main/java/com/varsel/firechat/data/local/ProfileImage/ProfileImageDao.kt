package com.varsel.firechat.data.local.ProfileImage

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProfileImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profileImage: ProfileImageEntity)

    @Update
    suspend fun update(profileImage: ProfileImageEntity)

    @Delete
    suspend fun delete(profileImage: ProfileImageEntity)

//    @Query("DELETE FROM profile_image_table WHERE imageId = :imageId")
//    fun deleteById(imageId: String): LiveData<Image>

    @Query("SELECT * FROM profile_image_table WHERE owner_id = :ownerId")
    fun get(ownerId: String): LiveData<ProfileImageEntity>

    // TODO: Add remove all and remove by id
}