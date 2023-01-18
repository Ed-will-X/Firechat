package com.varsel.firechat.domain.use_case.image

import android.content.Intent
import androidx.fragment.app.Fragment

val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
class OpenImagePicker_UseCase {
    operator fun invoke(fragment: Fragment) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        fragment.startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_SELECT_IMAGE_IN_ALBUM)
    }
}