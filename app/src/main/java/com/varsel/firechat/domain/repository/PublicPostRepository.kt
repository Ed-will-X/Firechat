package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.PublicPost.PublicPostEntity
import com.varsel.firechat.data.remote.dto.PublicPostDto
import com.varsel.firechat.domain.model.PublicPost

interface PublicPostRepository {

    suspend fun uploadPublicPost(publicPost: PublicPost, base64: String) : Response
    suspend fun getPublicPost(postId: String) : PublicPost

    // TODO: Undo after implementing remove public post id from user
//    suspend fun removePublicPost(publicPost: PublicPost) : Response

}