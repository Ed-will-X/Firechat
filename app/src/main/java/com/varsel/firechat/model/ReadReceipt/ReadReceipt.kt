package com.varsel.firechat.model.ReadReceipt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "read_receipts_table")
class ReadReceipt {
    @PrimaryKey(autoGenerate = false)
    lateinit var roomId: String

    var timestamp = 0L

    constructor()

    constructor(roomId: String, timestamp: Long){
        this.roomId = roomId
        this.timestamp = timestamp
    }
}