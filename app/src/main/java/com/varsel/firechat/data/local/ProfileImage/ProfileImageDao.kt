package com.varsel.firechat.data.local.ProfileImage

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProfileImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profileImage: ProfileImage)

    @Update
    suspend fun update(profileImage: ProfileImage)

    @Delete
    suspend fun delete(profileImage: ProfileImage)

//    @Query("DELETE FROM profile_image_table WHERE imageId = :imageId")
//    fun deleteById(imageId: String): LiveData<Image>

    @Query("SELECT * FROM profile_image_table WHERE owner_id = :ownerId")
    fun get_liveData(ownerId: String): LiveData<ProfileImage>

    @Query("SELECT * FROM profile_image_table WHERE owner_id = :ownerId")
    suspend fun get(ownerId: String): ProfileImage?

    // TODO: Add remove all and remove by id
}