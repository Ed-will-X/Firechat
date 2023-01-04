package com.varsel.firechat.domain.use_case.profile_image

import android.view.View
import com.varsel.firechat.R
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.repository.CurrentUserRepository
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.MessageUtils
import javax.inject.Inject

class DisplayProfileImage @Inject constructor(
    val currentUserRepository: CurrentUserRepository
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

            setOverlayBindings(image, imageOwnerFormat, activity.getString(R.string.profile_image), activity)
        }
    }

    fun setOverlayBindings(profileImage: ProfileImage, name: String, type: String, activity: SignedinActivity){
        activity.binding.imgOverlayParent.visibility = View.VISIBLE
        activity.binding.imgOverlayName.setText(name)
        activity.binding.imgOverlayType.setText(type)
        activity.binding.imgOverlayTimestamp.setText(MessageUtils.formatStampMessage(profileImage.imgChangeTimestamp.toString()))

        if(profileImage.image != null) {
            activity.binding.imgOverlayImage.setImageBitmap(ImageUtils.base64ToBitmap(profileImage.image!!))
        }
        // TODO: Hide status bar
        activity.hideStatusBar()

        activity.binding.imgOverlayBack.setOnClickListener {
            handleImgBackPress(activity)
        }

        activity.binding.imgOverlayParent.setOnClickListener {
            handleImgBackPress(activity)
        }
    }

    fun handleImgBackPress(activity: SignedinActivity){
        activity.binding.imgOverlayParent.visibility = View.GONE
        activity.binding.imgOverlayName.setText("")
        activity.binding.imgOverlayType.setText("")
        activity.binding.imgOverlayTimestamp.setText("")
        activity.showStatusBar()

        activity.binding.imgOverlayImage.setImageBitmap(null)
    }
}