package com.varsel.firechat.model.currentUser

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Update

@Dao
interface CurrentUserDao {
    @Update
    suspend fun updateCurrentUser(user: CurrentUser)

    @Delete
    suspend fun deleteCurrentUser(user: CurrentUser)

}