package com.varsel.firechat.domain.repository.remote

import com.varsel.firechat.data.local.Image.Image
import com.varsel.firechat.data.remote.dto.ImageDto

interface ChatImageRepository {
    fun uploadChatImage(image: Image, chatRoomID: String, base64: String, successCallback: (image: ImageDto)-> Unit, failureCallback: ()-> Unit)
    fun getChatImage(imageId: String, chatRoomID: String, image: (image: Image) -> Unit, afterCallback: () -> Unit)


}