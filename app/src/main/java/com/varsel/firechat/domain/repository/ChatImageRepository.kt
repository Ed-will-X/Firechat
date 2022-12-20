package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Image.ImageEntity
import com.varsel.firechat.domain.model.Image

interface ChatImageRepository {
    suspend fun uploadChatImage(image: Image, chatRoomID: String, base64: String) : Response
    suspend fun getChatImage(imageId: String, chatRoomID: String) : Image


}