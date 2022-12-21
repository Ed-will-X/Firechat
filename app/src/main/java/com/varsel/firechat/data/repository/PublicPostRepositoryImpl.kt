package com.varsel.firechat.data.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.PublicPost.PublicPost
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.PublicPostRepository
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PublicPostRepositoryImpl(
    val firebase: Firebase
) : PublicPostRepository {
    override suspend fun uploadPublicPost(publicPost: PublicPost, base64: String): Response = suspendCoroutine { continuation ->
        firebase.uploadPublicPost(publicPost, base64, {
            firebase.appendPublicPostIdToUser(publicPost.postId, {
                continuation.resume(Response.Success())
            }, {
                continuation.resume(Response.Fail())
            })
        }, {
            continuation.resume(Response.Fail())
        })
    }

    override suspend fun getPublicPost(postId: String): PublicPost = suspendCoroutine { continuation ->
        firebase.getPublicPost(postId, {
            continuation.resume(it)
        }, {})
    }
}