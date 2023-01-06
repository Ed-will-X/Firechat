package com.varsel.firechat.domain.use_case.public_post

import com.varsel.firechat.data.local.PublicPost.PublicPost
import com.varsel.firechat.domain.repository.PublicPostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPublicPostUseCase @Inject constructor(
    val repository: PublicPostRepository
) {
    suspend operator fun invoke(postId: String): Flow<PublicPost> {
        return repository.getPublicPost(postId)
    }
}