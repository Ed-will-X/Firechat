package com.varsel.firechat.data.local.ReadReceipt

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReadReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(readReceipt: ReadReceipt)

    @Delete
    suspend fun delete(readReceipt: ReadReceipt)

    @Update
    suspend fun update(readReceipt: ReadReceipt)

    @Query("SELECT * FROM read_receipts_table WHERE roomId = :id")
    fun get_liveData(id: String): LiveData<ReadReceipt>

    @Query("SELECT * FROM read_receipts_table WHERE roomId = :id")
    suspend fun get(id: String): ReadReceipt?
}