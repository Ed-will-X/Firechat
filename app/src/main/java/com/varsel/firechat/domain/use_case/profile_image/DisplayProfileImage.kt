package com.varsel.firechat.domain.use_case.profile_image

import com.varsel.firechat.R
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.domain.use_case._util.image.SetOverlayBindings_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import javax.inject.Inject

class DisplayProfileImage @Inject constructor(
    val currentUserRepository: CurrentUserRepository,
    val setoverlaybindingsUsecase: SetOverlayBindings_UseCase
) {
    operator fun invoke(image: ProfileImage, user: User, activity: SignedinActivity){
        val currentUserId = currentUserRepository.getCurrentUserId()

        activity.hideKeyboard()

        if(image.image != null){
            var imageOwnerFormat = ""

            if(image.ownerId.equals(currentUserId)){
                imageOwnerFormat = activity.getString(R.string.you)
            } else {
                imageOwnerFormat = user.name
            }

            setoverlaybindingsUsecase(image, imageOwnerFormat, activity.getString(R.string.profile_image), activity)
        }
    }


}