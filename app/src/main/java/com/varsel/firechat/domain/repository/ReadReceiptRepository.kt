package com.varsel.firechat.domain.repository

import com.varsel.firechat.data.local.ReadReceipt.ReadReceipt

interface ReadReceiptRepository {
    suspend fun storeReceipt(readReceipt: ReadReceipt)
    suspend fun fetchReceipt(id: String): ReadReceipt?
}