package com.varsel.firechat.domain.use_case.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class CheckIfCameraPermissionGranted_UseCase {
    operator fun invoke(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
}