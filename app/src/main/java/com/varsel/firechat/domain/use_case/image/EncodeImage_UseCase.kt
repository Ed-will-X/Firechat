package com.varsel.firechat.domain.use_case.image

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

class EncodeImage_UseCase {
    operator fun invoke(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }
}