package com.varsel.firechat.domain.use_case.chat_image

import android.net.Uri
import com.varsel.firechat.R
import com.varsel.firechat.data.local.Image.Image
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.Message.MessageType
import com.varsel.firechat.domain.use_case._util.InfobarColors
import com.varsel.firechat.domain.use_case._util.message.GenerateUid_UseCase
import com.varsel.firechat.domain.use_case.image.EncodeUri_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.common._utils.MessageUtils
import javax.inject.Inject

class UploadChatImage_UseCase @Inject constructor(
    val encodeUri: EncodeUri_UseCase,
    val generateUID: GenerateUid_UseCase
) {
    operator fun invoke(uri: Uri, chatRoomId: String, activity: SignedinActivity, success: (message: Message, image: Image)-> Unit){
        val encoded = encodeUri(uri, activity)
        val imageId = MessageUtils.generateUID()

        if(encoded != null){

            // TODO: Change owner id from current user to current chat room
            val image = Image(imageId, activity.firebaseAuth.currentUser!!.uid)
            val message = Message(MessageUtils.generateUID(), imageId, System.currentTimeMillis(), activity.firebaseAuth.currentUser!!.uid, MessageType.IMAGE)

            // Shows bottom infobar
            activity.infobarController.showBottomInfobar(activity.getString(R.string.uploading_chat_image), InfobarColors.UPLOADING)

            activity.firebaseViewModel.uploadChatImage(image, chatRoomId, encoded, activity.firebaseStorage, activity.mDbRef, {
                activity.infobarController.showBottomInfobar(activity.getString(R.string.chat_image_upload_successful), InfobarColors.SUCCESS)

                success(message, it)
            }, {
                activity.infobarController.showBottomInfobar(activity.getString(R.string.chat_image_upload_error), InfobarColors.FAILURE)
            })
        }
    }
}