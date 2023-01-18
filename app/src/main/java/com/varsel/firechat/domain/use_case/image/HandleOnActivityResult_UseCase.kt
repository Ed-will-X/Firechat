package com.varsel.firechat.domain.use_case.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import javax.inject.Inject

val REQUEST_TAKE_PHOTO = 0
class HandleOnActivityResult_UseCase @Inject constructor(
    val encodeImage: EncodeImage_UseCase
) {
    operator fun invoke(requestCode: Int, resultCode: Int, data: Intent?, albumCallback: (uri: Uri)-> Unit, cameraCallback: (imageEncoded: String?)-> Unit){
        if(resultCode != Activity.RESULT_CANCELED){
            if(requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM && resultCode == Activity.RESULT_OK){
                var uri: Uri? = data?.data
                if(uri != null) {
                    albumCallback(uri)
                }
            }
            else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK){
                val image: Bitmap = data?.extras?.get("data") as Bitmap
                // TODO: Fix overcompression bug
                val base64: String? = encodeImage(image)
                cameraCallback(base64)

            }
        }
    }
}