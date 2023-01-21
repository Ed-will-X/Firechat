package com.varsel.firechat.domain.use_case.read_receipt

import com.varsel.firechat.data.local.ReadReceipt.ReadReceipt
import com.varsel.firechat.domain.repository.ReadReceiptRepository
import javax.inject.Inject

class FetchReceipt_UseCase @Inject constructor(
    val repository: ReadReceiptRepository
) {
    suspend operator fun invoke(id: String): ReadReceipt? {
        return repository.fetchReceipt(id)
    }
}