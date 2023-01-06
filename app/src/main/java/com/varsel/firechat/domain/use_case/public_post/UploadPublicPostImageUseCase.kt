package com.varsel.firechat.domain.use_case.public_post

import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.PublicPost.PublicPost
import com.varsel.firechat.domain.repository.PublicPostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadPublicPostImageUseCase @Inject constructor(
    val repository: PublicPostRepository
) {
    suspend operator fun invoke(publicPost: PublicPost, base64: String): Flow<Response> {
        if(publicPost.image != null) {
            val publicPost_withoutImage = PublicPost(publicPost.ownerId, publicPost.postId, publicPost.type, publicPost.caption, publicPost.postTimestamp)
            return repository.uploadPublicPost(publicPost_withoutImage, base64)
        }
        return repository.uploadPublicPost(publicPost, base64)
    }
}