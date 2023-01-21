package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Image.Image
import kotlinx.coroutines.flow.Flow

interface ChatImageRepository {
    suspend fun uploadChatImage(image: Image, chatRoomID: String, base64: String) : Flow<Response>
    suspend fun getChatImage(imageId: String, chatRoomID: String) : Flow<Resource<Image>>
    suspend fun checkIfImageInDb(imageId: String): Flow<Resource<Image?>>
    suspend fun storeImage(image: Image)
}