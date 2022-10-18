package com.varsel.firechat.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.View
import android.widget.ImageView
import com.google.android.material.card.MaterialCardView
import com.varsel.firechat.model.Image.Image
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream


class ImageUtils {
    companion object {
        // returns a base64 string
        fun encodeImage(bm: Bitmap): String? {
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val b: ByteArray = baos.toByteArray()
            return Base64.encodeToString(b, Base64.DEFAULT)
        }

        fun uriToBitmap(imageUri: Uri, activity: Activity): String? {
            val imageStream: InputStream? = activity.getContentResolver().openInputStream(imageUri)
            val selectedImage = BitmapFactory.decodeStream(imageStream)
            val encodedImage = encodeImage(selectedImage)

            return encodedImage
        }

        // for the received images
        fun base64ToBitmap(encodedImage: String): Bitmap{
            val decodedString: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            val decoded = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            return decoded
        }

        fun setProfileImage(image: Image, imageCard: MaterialCardView, imageView: ImageView){
            if(image.image != null){
                val decodedImage = ImageUtils.base64ToBitmap(image.image!!)
                imageCard.visibility = View.VISIBLE
                imageView.setImageBitmap(decodedImage)
            } else {
                imageCard.visibility = View.GONE
            }
        }

        fun setProfilePic(base64: String, view: ImageView, viewParent: View){
            val bitmap = ImageUtils.base64ToBitmap(base64)

            view.setImageBitmap(bitmap)
            viewParent.visibility = View.VISIBLE
        }

        fun setProfilePicOtherUser(user: User, view: ImageView, viewParent: View, parent: SignedinActivity){
            parent.determineOtherImgFetchMethod(user, {
                if (it != null) {
                    setProfilePic(it, view, viewParent)
                }
            }, {
                if (it != null) {
                    setProfilePic(it, view, viewParent)
                }
            })
        }

    }
}