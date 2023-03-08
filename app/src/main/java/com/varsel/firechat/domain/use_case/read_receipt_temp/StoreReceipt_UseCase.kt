package com.varsel.firechat.domain.use_case.read_receipt_temp

import com.varsel.firechat.data.local.ReadReceipt.ReadReceipt
import com.varsel.firechat.domain.repository.ReadReceiptRepository
import javax.inject.Inject

class StoreReceipt_UseCase @Inject constructor(
    val repository: ReadReceiptRepository
) {
    suspend operator fun invoke(readReceipt: ReadReceipt) {
        repository.storeReceipt(readReceipt)
    }
}