package com.varsel.firechat.data.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Image.Image
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.ChatImageRepository
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChatImageRepositoryImpl @Inject constructor(
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
            continuation.resume(it)
        }, {})
    }

}