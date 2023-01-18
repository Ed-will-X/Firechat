package com.varsel.firechat.domain.use_case.public_post

import com.varsel.firechat.R
import com.varsel.firechat.data.local.PublicPost.PublicPost
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case._util.image.SetOverlayBindings_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import javax.inject.Inject

class DisplayPublicPostImage_UseCase @Inject constructor(
    val setoverlaybindingsUsecase: SetOverlayBindings_UseCase
) {

    operator fun invoke(post: PublicPost, user: User, activity: SignedinActivity){
        if(post.image != null){
            var imageOwnerFormat = user.name

            setoverlaybindingsUsecase(post, imageOwnerFormat, activity.getString(R.string.public_post_image), activity)
        }
    }
}