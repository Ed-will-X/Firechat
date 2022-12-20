package com.varsel.firechat.data.local.PublicPost

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PublicPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(publicPost: PublicPostEntity)

    @Update
    suspend fun update(publicPost: PublicPostEntity)

    @Delete
    suspend fun delete(publicPost: PublicPostEntity)

    @Query("SELECT * FROM public_post_table WHERE postId = :postId")
    fun get(postId: String): LiveData<PublicPostEntity>
}