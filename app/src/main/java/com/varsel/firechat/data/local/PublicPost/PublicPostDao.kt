package com.varsel.firechat.data.local.PublicPost

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PublicPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(publicPost: PublicPost)

    @Update
    suspend fun update(publicPost: PublicPost)

    @Delete
    suspend fun delete(publicPost: PublicPost)

    @Query("SELECT * FROM public_post_table WHERE postId = :postId")
    fun get_liveData(postId: String): LiveData<PublicPost>

    @Query("SELECT * FROM public_post_table WHERE postId = :postId")
    suspend fun get(postId: String): PublicPost?
}