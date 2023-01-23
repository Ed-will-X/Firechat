package com.varsel.firechat.domain.use_case._util.image

import android.view.View
import com.varsel.firechat.common._utils.ImageUtils
import com.varsel.firechat.data.local.Image.Image
import com.varsel.firechat.data.local.Message.Message
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.PublicPost.PublicPost
import com.varsel.firechat.domain.use_case._util.message.FormatStampMessage_UseCase
import com.varsel.firechat.domain.use_case._util.status_bar.SetStatusBarVisibility_UseCase
import com.varsel.firechat.domain.use_case._util.status_bar.StatusBarVisibility
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import javax.inject.Inject

class SetOverlayBindings_UseCase @Inject constructor(
    val formatStampMessage: FormatStampMessage_UseCase,
    val setStatusBarVisibility: SetStatusBarVisibility_UseCase
) {
    operator fun invoke(profileImage: ProfileImage, name: String, type: String, activity: SignedinActivity){
        activity.binding.imgOverlayParent.visibility = View.VISIBLE
        activity.binding.imgOverlayName.setText(name)
        activity.binding.imgOverlayType.setText(type)
        activity.binding.imgOverlayTimestamp.setText(formatStampMessage(profileImage.imgChangeTimestamp.toString()))

        if(profileImage.image != null) {
            activity.binding.imgOverlayImage.setImageBitmap(ImageUtils.base64ToBitmap(profileImage.image!!))
        }
        setStatusBarVisibility(StatusBarVisibility.Hide(), activity)

        activity.binding.imgOverlayBack.setOnClickListener {
            handleImgBackPress(activity)
        }

        activity.binding.imgOverlayParent.setOnClickListener {
            handleImgBackPress(activity)
        }
    }

    operator fun invoke(chatImage: Image, name: String, type: String, message: Message, activity: SignedinActivity){
        activity.binding.imgOverlayParent.visibility = View.VISIBLE
        activity.binding.imgOverlayName.setText(name)
        activity.binding.imgOverlayType.setText(type)
        activity.binding.imgOverlayTimestamp.setText(formatStampMessage(message.time.toString()))

        if(chatImage.image != null) {
            activity.binding.imgOverlayImage.setImageBitmap(ImageUtils.base64ToBitmap(chatImage.image!!))
        }
        setStatusBarVisibility(StatusBarVisibility.Hide(), activity)

        activity.binding.imgOverlayBack.setOnClickListener {
            handleImgBackPress(activity)
        }

        activity.binding.imgOverlayParent.setOnClickListener {
            handleImgBackPress(activity)
        }
    }

    operator fun invoke(publicPost: PublicPost, name: String, type: String, activity: SignedinActivity){
        activity.binding.imgOverlayParent.visibility = View.VISIBLE
        activity.binding.imgOverlayName.setText(name)
        activity.binding.imgOverlayType.setText(type)
        activity.binding.imgOverlayTimestamp.setText(formatStampMessage(publicPost.postTimestamp.toString()))

        if(publicPost.image != null) {
            activity.binding.imgOverlayImage.setImageBitmap(ImageUtils.base64ToBitmap(publicPost.image!!))
        }

        setStatusBarVisibility(StatusBarVisibility.Hide(), activity)

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
        setStatusBarVisibility(StatusBarVisibility.Show(), activity)

        activity.binding.imgOverlayImage.setImageBitmap(null)
    }
}