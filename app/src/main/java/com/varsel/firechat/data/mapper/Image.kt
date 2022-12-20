package com.varsel.firechat.data.mapper

import com.varsel.firechat.data.local.Image.ImageEntity
import com.varsel.firechat.data.remote.dto.ImageDto
import com.varsel.firechat.domain.model.Image

fun ImageEntity.toImage() : Image {
    return Image(
        imageId = imageId,
        image = image!!,
        ownerId = ownerId
    )
}

fun ImageDto.toImage() : Image {
    return Image(
        imageId = imageId,
        image = image,
        ownerId = ownerId
    )
}