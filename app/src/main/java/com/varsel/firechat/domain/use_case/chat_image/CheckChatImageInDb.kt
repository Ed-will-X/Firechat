package com.varsel.firechat.domain.use_case.chat_image

import com.varsel.firechat.common.Resource
import com.varsel.firechat.data.local.Image.Image
import com.varsel.firechat.domain.repository.ChatImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckChatImageInDb @Inject constructor(
    val repository: ChatImageRepository
) {
    suspend operator fun invoke(imageId: String): Flow<Resource<Image?>> {
        return repository.checkIfImageInDb(imageId)
    }
}