package com.varsel.firechat.domain.use_case.chat_image

import com.varsel.firechat.R
import com.varsel.firechat.data.local.Image.Image
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.domain.use_case._util.image.SetOverlayBindings_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import javax.inject.Inject

class DisplayChatImage_UseCase @Inject constructor(
    val currentUserRepository: CurrentUserRepository,
    val setoverlaybindingsUsecase: SetOverlayBindings_UseCase
) {
    operator fun invoke(image: Image, message: Message, activity: SignedinActivity){
        val otherUser = activity.firebaseViewModel.selectedChatRoomUser.value
        val currentUserId = currentUserRepository.getCurrentUserId()

        activity.hideKeyboard()

        if(otherUser != null){
            var imageOwnerFormat = ""

            if(image.ownerId.equals(currentUserId)){
                imageOwnerFormat = activity.getString(R.string.from_you)
            } else {
                imageOwnerFormat = activity.getString(R.string.from_user, otherUser.name)
            }

            setoverlaybindingsUsecase(image, imageOwnerFormat, activity.getString(R.string.chat_image), message, activity)
        }
    }

}