package com.varsel.firechat.domain.use_case.chat_image

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Image.Image
import com.varsel.firechat.domain.repository.ChatImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StoreChatImageUseCase @Inject constructor(
    val repository: ChatImageRepository
) {
    suspend operator fun invoke(image: Image) {
        return repository.storeImage(image)
    }
}