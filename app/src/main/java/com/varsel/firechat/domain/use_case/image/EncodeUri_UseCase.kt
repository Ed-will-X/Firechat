package com.varsel.firechat.domain.use_case.image

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import java.io.InputStream
import javax.inject.Inject

class EncodeUri_UseCase @Inject constructor(
    val checkImageDimensions: CheckImageDimensions_UseCase,
    val encodeImage: EncodeImage_UseCase,
    val resizeImage: ResizeImageUseCase
) {
    // TODO: Replace with resource strings
    operator fun invoke(imageUri: Uri, activity: Activity): String? {
        val imageStream: InputStream? = activity.getContentResolver().openInputStream(imageUri)
        val selectedImage = BitmapFactory.decodeStream(imageStream)

        var encodedImage: String? = null
        checkImageDimensions(selectedImage, {
            val base64: String? = encodeImage(selectedImage)
            encodedImage = base64
        }, {
            // Show toast
            Toast.makeText(activity, "Image too large", Toast.LENGTH_SHORT).show()

            encodedImage = null
        }, {
            val resized = resizeImage(selectedImage)
            val base64: String? = encodeImage(resized)
            encodedImage = base64
        })

        return encodedImage
    }
}