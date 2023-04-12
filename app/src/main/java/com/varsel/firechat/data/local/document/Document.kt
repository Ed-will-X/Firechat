package com.varsel.firechat.data.local.document

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_table")
class Document {
    @PrimaryKey(autoGenerate = false)
    lateinit var document_id: String

    lateinit var owner_id: String

    lateinit var extension_type: String

    var file_content: String? = null

    constructor()

    constructor(owner_id: String, document_id: String) {
        this.document_id = document_id
        this.owner_id = owner_id
    }
}