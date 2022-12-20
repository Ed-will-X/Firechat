package com.varsel.firechat.data.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Image.ImageEntity
import com.varsel.firechat.data.mapper.toImage
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.data.remote.dto.ImageDto
import com.varsel.firechat.domain.model.Image
import com.varsel.firechat.domain.repository.ChatImageRepository
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChatImageRepositoryImpl(
    val firebase: Firebase
) : ChatImageRepository {
    override suspend fun uploadChatImage(image: Image, chatRoomID: String, base64: String): Response = suspendCoroutine { continuation ->
        firebase.uploadChatImage(image, chatRoomID, base64, {
            continuation.resume(Response.Success())
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun getChatImage(imageId: String, chatRoomID: String): Image = suspendCoroutine { continuation ->
        firebase.getChatImage(imageId, chatRoomID, {
            continuation.resume(it.toImage())
        }, {})
    }

}