package com.varsel.firechat.domain.use_case.chat_image

import com.varsel.firechat.R
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.domain.use_case._util.image.SetOverlayBindings_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import javax.inject.Inject

class DisplayGroupImage_UseCase @Inject constructor(
    val setoverlaybindingsUsecase: SetOverlayBindings_UseCase
) {
    operator fun invoke(image: ProfileImage, group: GroupRoom, activity: SignedinActivity){
        activity.hideKeyboard()

        if(image.image != null){
            var imageOwnerFormat = group.groupName.toString()

            setoverlaybindingsUsecase(image, imageOwnerFormat, activity.getString(R.string.group_image), activity)
        }
    }
}