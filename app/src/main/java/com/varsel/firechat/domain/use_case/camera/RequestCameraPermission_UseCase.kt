package com.varsel.firechat.domain.use_case.camera

import android.Manifest
import androidx.core.app.ActivityCompat
import com.varsel.firechat.presentation.signedIn.SignedinActivity

class RequestCameraPermission_UseCase {
    operator fun invoke(activity: SignedinActivity){
        ActivityCompat.requestPermissions(
            activity,
            arrayOf<String>(Manifest.permission.CAMERA),
            1
        )
    }
}