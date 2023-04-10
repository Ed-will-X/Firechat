package com.varsel.firechat.domain.use_case.chat_image

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.varsel.firechat.common._utils.ImageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity

class SetChatImageUseCase {
    operator fun invoke(base64: String, image: ImageView, viewParent: FrameLayout, activity: SignedinActivity){
        if(base64.isNotEmpty()){
            val bitmap = ImageUtils.base64ToBitmap(base64)
            Glide.with(activity).load(bitmap).dontAnimate().into(image)
            viewParent.visibility = View.VISIBLE

//                image.setImageBitmap(bitmap)
        }
    }
}