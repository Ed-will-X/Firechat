package com.varsel.firechat.domain.use_case.image

import android.graphics.Bitmap
import android.util.Log

class CheckImageDimensions_UseCase {
    operator fun invoke(image: Bitmap, withinBounds: ()-> Unit, outOfBounds: ()-> Unit, compressCallback: ()-> Unit){
        // TODO: Add for image upload and experiment for FHD
        Log.d("COMPRESS", "width: ${image.width}")
        Log.d("COMPRESS", "height: ${image.height}")
        if(image.height < 1280 && image.width < 1280){
            Log.d("COMPRESS", "Within bounds")
            withinBounds()
        } else if(image.height > 8000 || image.width > 8000) {
            Log.d("COMPRESS", "out of bounds")
            outOfBounds()
        } else {
            // minor compress
            Log.d("COMPRESS", "Minor compress callback")
            compressCallback()
        }
    }
}