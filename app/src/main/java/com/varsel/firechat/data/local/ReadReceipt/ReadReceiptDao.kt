package com.varsel.firechat.data.local.ReadReceipt

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReadReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(readReceipt: ReadReceiptEntity)

    @Delete
    suspend fun delete(readReceipt: ReadReceiptEntity)

    @Update
    suspend fun update(readReceipt: ReadReceiptEntity)

    @Query("SELECT * FROM read_receipts_table WHERE roomId = :roomId")
    fun get(roomId: String): LiveData<ReadReceiptEntity>
}