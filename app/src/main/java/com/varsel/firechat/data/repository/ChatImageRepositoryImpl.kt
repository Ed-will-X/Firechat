package com.varsel.firechat.data.repository

import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Image.Image
import com.varsel.firechat.data.local.Image.ImageDao
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.ChatImageRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChatImageRepositoryImpl @Inject constructor(
    val firebase: Firebase,
    val imageDao: ImageDao
) : ChatImageRepository {
    override suspend fun uploadChatImage(image: Image, chatRoomID: String, base64: String): Flow<Response> = callbackFlow {
        firebase.uploadChatImage(image, chatRoomID, base64, {
            runBlocking { imageDao.insert(it) }
            trySend(Response.Success())
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override suspend fun getChatImage(imageId: String, chatRoomID: String): Flow<Resource<Image>> = callbackFlow {
        trySend(Resource.Loading())
        val image = imageDao.get(imageId)

        if(image != null) {
            trySend(Resource.Success(image))
        } else {
            firebase.getChatImage(imageId, chatRoomID, {
                trySend(Resource.Success(it))

                runBlocking { imageDao.insert(it) }
            }, {  })
        }

        awaitClose {  }
    }

    override suspend fun checkIfImageInDb(imageId: String): Flow<Resource<Image?>> = callbackFlow {
        trySend(Resource.Loading())
        val image = imageDao.get(imageId)

        if(image != null) {
            trySend(Resource.Success(image))
        } else {
            trySend(Resource.Error("Image not found in local storage"))
        }

        awaitClose {  }
    }

}