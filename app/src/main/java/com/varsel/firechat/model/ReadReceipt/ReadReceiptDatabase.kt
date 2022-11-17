package com.varsel.firechat.model.ReadReceipt

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReadReceipt::class], version = 1, exportSchema = false)
abstract class ReadReceiptDatabase: RoomDatabase() {
    abstract val readReceiptDao: ReadReceiptDao

    companion object {
        @Volatile
        private var INSTANCE: ReadReceiptDatabase? = null

        fun getInstance(context: Context): ReadReceiptDatabase {
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext, ReadReceiptDatabase:: class.java, "read_receipts_database").build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}