package com.varsel.firechat.domain.repository.remote

import com.varsel.firechat.data.local.PublicPost.PublicPost
import com.varsel.firechat.data.remote.dto.PublicPostDto

interface PublicPostRepository {

    fun uploadPublicPost(publicPost: PublicPost, base64: String, successCallback: (publicPost: PublicPostDto)-> Unit, failureCallback: ()-> Unit)
    fun removePublicPost(publicPost: PublicPost, success: ()-> Unit, failure: ()-> Unit)
    fun getPublicPost(postId: String, loopCallback: (publicPost: PublicPost) -> Unit, afterCallback: () -> Unit)
    fun appendPublicPostIdToUser(postId: String, success: () -> Unit, fail: () -> Unit)


}