package com.varsel.firechat.model.PublicPost

import androidx.lifecycle.LiveData
import androidx.room.*
import com.varsel.firechat.model.ProfileImage.ProfileImage

@Dao
interface PublicPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(publicPost: PublicPost)

    @Update
    suspend fun update(publicPost: PublicPost)

    @Delete
    suspend fun delete(publicPost: PublicPost)

    @Query("SELECT * FROM public_post_table WHERE postId = :postId")
    fun get(postId: String): LiveData<PublicPost>
}