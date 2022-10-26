package com.varsel.firechat.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream

val REQUEST_TAKE_PHOTO = 0
val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
class ImageUtils {
    companion object {
        fun openImagePicker(fragment: Fragment){
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            fragment.startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_SELECT_IMAGE_IN_ALBUM)
        }

        fun openCamera(context: Context, fragment: Fragment, activiy: SignedinActivity){
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if(isCameraPermissionGranted(context)){
                fragment.startActivityForResult(intent, REQUEST_TAKE_PHOTO)
            } else {
                requestCameraPermission(activiy)
            }
        }

        private fun requestCameraPermission(activity: SignedinActivity){
            ActivityCompat.requestPermissions(
                activity,
                arrayOf<String>(Manifest.permission.CAMERA),
                1
            )
        }

        private fun isCameraPermissionGranted(context: Context): Boolean{
            return ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        }

        fun handleOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?, albumCallback: (uri: Uri)-> Unit, cameraCallback: (imageEncoded: String?)-> Unit){
            if(requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM && resultCode == Activity.RESULT_OK){
                var uri: Uri? = data?.data
                if(uri != null) {
                    albumCallback(uri)
                }
            }
            else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK){
                val image: Bitmap = data?.extras?.get("data") as Bitmap

                if(image != null) {
                    val base64: String? = ImageUtils.encodeImage(image)
                    cameraCallback(base64)
                }
            }
        }




        // returns a base64 string
        fun encodeImage(bm: Bitmap): String? {
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 50, baos)
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

        fun setProfilePicOtherUser(user: User, view: ImageView, viewParent: View, parent: SignedinActivity, imgCallback: (image: String?)-> Unit){
            parent.determineOtherImgFetchMethod(user, {
                if (it != null) {
                    imgCallback(it)
                    setProfilePic(it, view, viewParent)
                } else {
                    imgCallback(null)
                }
            }, {
                if (it != null) {
                    imgCallback(it)
                    setProfilePic(it, view, viewParent)
                } else {
                    imgCallback(null)
                }
            })
        }

        fun setProfilePicGroup(group: GroupRoom, view: ImageView, viewParent: View, parent: SignedinActivity, imgCallback: (image: String?)-> Unit){
            parent.determineGroupFetchMethod(group, {
                if (it != null) {
                    imgCallback(it)
                    setProfilePic(it, view, viewParent)
                } else {
                    imgCallback(null)
                }
            }, {
                if (it != null) {
                    imgCallback(it)
                    setProfilePic(it, view, viewParent)
                } else {
                    imgCallback(null)
                }
            })
        }


    }
}