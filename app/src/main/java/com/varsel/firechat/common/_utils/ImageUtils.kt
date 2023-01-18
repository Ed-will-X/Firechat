package com.varsel.firechat.common._utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

class ImageUtils {
    companion object {
        // for the received images
        fun base64ToBitmap(encodedImage: String): Bitmap {
            val decodedString: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            val decoded = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            return decoded
        }

        fun base64ToByteArray(encodedImage: String): ByteArray{
            val byteArray: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)

            return byteArray
        }

        fun byteArraytoBase64(byteArray: ByteArray): String{
            val encoded: String = Base64.encodeToString(byteArray, Base64.DEFAULT)
            return encoded
        }
    }
}