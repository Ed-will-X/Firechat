package com.varsel.firechat.data.repository

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.PublicPost.PublicPost
import com.varsel.firechat.data.local.PublicPost.PublicPostDao
import com.varsel.firechat.data.remote.Firebase
import com.varsel.firechat.domain.repository.PublicPostRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class PublicPostRepositoryImpl @Inject constructor(
    val firebase: Firebase,
    val dao: PublicPostDao
) : PublicPostRepository {
    override suspend fun uploadPublicPost(publicPost: PublicPost, base64: String): Flow<Response> = callbackFlow {
        firebase.uploadPublicPost(publicPost, base64, {
            firebase.appendPublicPostIdToUser(publicPost.postId, {
                trySend(Response.Success())
                runBlocking { dao.insert(it) }
            }, {
                trySend(Response.Fail())
            })
        }, {
            trySend(Response.Fail())
        })

        awaitClose {  }
    }

    override suspend fun getPublicPost(postId: String): Flow<PublicPost> = callbackFlow {
        val post = dao.get(postId)

        if (post != null) {
            trySend(post)
        } else {
            firebase.getPublicPost(postId, {
                trySend(it)
                runBlocking { dao.insert(it) }
            }, {})
        }

        awaitClose {  }
    }

    override suspend fun isPostInRoom(postId: String): Flow<PublicPost?> = callbackFlow {
        val post = dao.get(postId)

        if (post != null) {
            trySend(post)
        } else {
            trySend(null)
        }

        awaitClose {  }
    }
}