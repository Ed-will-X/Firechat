package com.varsel.firechat.domain.use_case.image

import android.graphics.Bitmap
import android.util.Log

class ResizeImageUseCase {
    operator fun invoke(image: Bitmap): Bitmap {

        val width = image.width
        val height = image.height
        var aspectRatio: Float = 0F

        // check the lower value before division
        if(image.width != image.height){
            val max = maxOf(image.width, image.height)
            val min = minOf(image.width, image.height)
            aspectRatio = min.toFloat() / max.toFloat()
            var scaledWidth = 0
            var scaledHeight = 0

            Log.d("COMPRESS", "----------------------------------------------------------")

            Log.d("COMPRESS", "Bitmap size: ${image.byteCount}")

            Log.d("COMPRESS", "old Bitmap width: ${image.width}")
            Log.d("COMPRESS", "old bitmap height: ${image.height}")

            if (image.byteCount <= 200_000){
                return image
            }

            if(width > height){
                scaledWidth = 1280
                scaledHeight = (1280 * aspectRatio).toInt()
            } else {
                scaledWidth  = (1280 * aspectRatio).toInt()
                scaledHeight = 1280
            }


            val scaledBitmap = Bitmap.createScaledBitmap(image, scaledWidth, scaledHeight, false)

            Log.d("COMPRESS", "Scaled bitmap size: ${scaledBitmap.byteCount}")

            Log.d("COMPRESS", "new Bitmap width: ${scaledBitmap.width}")
            Log.d("COMPRESS", "new bitmap height: ${scaledBitmap.height}")

            return scaledBitmap

        } else {
            // When the width and height are the same
            val scaledBitmap = Bitmap.createScaledBitmap(image, 1280, 1280, false)

            Log.d("COMPRESS", "Scaled bitmap size: ${scaledBitmap.byteCount}")

            Log.d("COMPRESS", "new Bitmap width: ${scaledBitmap.width}")
            Log.d("COMPRESS", "new bitmap height: ${scaledBitmap.height}")

            return scaledBitmap
        }
    }
}