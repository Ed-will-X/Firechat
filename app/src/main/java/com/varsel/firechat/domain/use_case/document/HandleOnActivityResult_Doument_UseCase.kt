package com.varsel.firechat.domain.use_case.document

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.varsel.firechat.domain.use_case.image.EncodeImage_UseCase
import com.varsel.firechat.domain.use_case.image.REQUEST_SELECT_IMAGE_IN_ALBUM
import com.varsel.firechat.domain.use_case.image.REQUEST_TAKE_PHOTO
import javax.inject.Inject

class HandleOnActivityResult_Doument_UseCase @Inject constructor(

) {
    operator fun invoke(requestCode: Int, resultCode: Int, data: Intent?, callback: (uri: Uri?)-> Unit){
        if (requestCode == PICK_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if(uri != null) {
                callback(uri)
            }
        }
    }
}