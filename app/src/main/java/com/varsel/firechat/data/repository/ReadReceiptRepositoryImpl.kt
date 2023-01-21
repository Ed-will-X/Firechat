package com.varsel.firechat.data.repository

import com.varsel.firechat.data.local.ReadReceipt.ReadReceipt
import com.varsel.firechat.data.local.ReadReceipt.ReadReceiptDao
import com.varsel.firechat.domain.repository.ReadReceiptRepository
import javax.inject.Inject

class ReadReceiptRepositoryImpl @Inject constructor(
    val dao: ReadReceiptDao
): ReadReceiptRepository {
    override suspend fun storeReceipt(readReceipt: ReadReceipt) {
        dao.insert(readReceipt)
    }

    override suspend fun fetchReceipt(id: String): ReadReceipt? {
        return dao.get(id)
    }
}