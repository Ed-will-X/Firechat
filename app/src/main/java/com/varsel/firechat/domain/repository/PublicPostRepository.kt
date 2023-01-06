package com.varsel.firechat.domain.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.PublicPost.PublicPost
import kotlinx.coroutines.flow.Flow

interface PublicPostRepository {

    suspend fun uploadPublicPost(publicPost: PublicPost, base64: String) : Flow<Response>
    suspend fun getPublicPost(postId: String) : Flow<PublicPost>

    suspend fun isPostInRoom(postId: String): Flow<PublicPost?>

    // TODO: Undo after implementing remove public post id from user
//    suspend fun removePublicPost(publicPost: PublicPost) : Response

}