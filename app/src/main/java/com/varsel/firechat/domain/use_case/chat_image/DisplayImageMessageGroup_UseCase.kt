package com.varsel.firechat.domain.use_case.chat_image

import com.varsel.firechat.R
import com.varsel.firechat.data.local.Image.Image
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.domain.repository.MessageRepository
import com.varsel.firechat.domain.repository.OtherUserRepository
import com.varsel.firechat.domain.use_case._util.image.SetOverlayBindings_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import javax.inject.Inject

class DisplayImageMessageGroup_UseCase @Inject constructor(
    val currentUserRepository: CurrentUserRepository,
    val setoverlaybindingsUsecase: SetOverlayBindings_UseCase,
) {
    operator fun invoke(image: Image, message: Message, activity: SignedinActivity){
        val currentUserId = currentUserRepository.getCurrentUserId()
        // TODO: Fix bug here:
        val selectedGroupRoomParticipants = activity.firebaseViewModel.selectedGroupParticipants.value

        activity.hideKeyboard()

        var imageOwnerFormat = ""
        if(selectedGroupRoomParticipants != null){
            var selectedUser: User? = null
            for(i in selectedGroupRoomParticipants){
                if(i.userUID == image.ownerId){
                    selectedUser = i
                }
            }
            if(selectedUser != null){
                if(image.ownerId.equals(currentUserId)){
                    imageOwnerFormat = activity.getString(R.string.from_you)
                } else {
                    imageOwnerFormat = activity.getString(R.string.from_user, selectedUser.name)
                }

            }
        }

        setoverlaybindingsUsecase(image, imageOwnerFormat, activity.getString(R.string.group_image), message, activity)

    }
}