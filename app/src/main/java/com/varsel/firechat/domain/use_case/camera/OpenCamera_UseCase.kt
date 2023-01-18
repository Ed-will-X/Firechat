package com.varsel.firechat.domain.use_case.camera

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import javax.inject.Inject

val REQUEST_TAKE_PHOTO = 0
class OpenCamera_UseCase @Inject constructor(
    val checkCameraPermission: CheckIfCameraPermissionGranted_UseCase,
    val requestCameraPermission: RequestCameraPermission_UseCase
) {
    operator fun invoke(context: Context, fragment: Fragment, activiy: SignedinActivity){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if(checkCameraPermission(context)){
            fragment.startActivityForResult(intent, REQUEST_TAKE_PHOTO)
        } else {
            requestCameraPermission(activiy)
        }
    }
}