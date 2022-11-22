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
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.varsel.firechat.R
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.Image.Image
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.Message.MessageType
import com.varsel.firechat.model.ProfileImage.ProfileImage
import com.varsel.firechat.model.PublicPost.PublicPost
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream


val REQUEST_TAKE_PHOTO = 0
val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
class ImageUtils {
    companion object {
        val FHD_width = 1920
        val FHD_height = 1920
        val HD_width = 720
        val HD_height = 720

        val img_limit_width = 4000
        val img_limit_height = 4000

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

        fun handleOnActivityResult(context: Context, requestCode: Int, resultCode: Int, data: Intent?, albumCallback: (uri: Uri)-> Unit, cameraCallback: (imageEncoded: String?)-> Unit){
            // TODO: Re-test camera callback because of the new conditional
            if(resultCode != Activity.RESULT_CANCELED){
                if(requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM && resultCode == Activity.RESULT_OK){
                    var uri: Uri? = data?.data
                    if(uri != null) {
                        albumCallback(uri)
                    }
                }
                else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK){
                    val image: Bitmap = data?.extras?.get("data") as Bitmap
                    // TODO: Fix overcompression bug
                    val base64: String? = ImageUtils.encodeImage(image)
                    cameraCallback(base64)

                }
            }
        }

        private fun checkImageDimensions(image: Bitmap, withinBounds: ()-> Unit, outOfBounds: ()-> Unit, compressCallback: ()-> Unit){
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

        // returns a base64 string and compresses
        fun encodeImage(bm: Bitmap): String? {
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val b: ByteArray = baos.toByteArray()
            return Base64.encodeToString(b, Base64.DEFAULT)
        }

        fun encodeImage(){

        }

        fun encodeUri(imageUri: Uri, activity: Activity): String? {
            val imageStream: InputStream? = activity.getContentResolver().openInputStream(imageUri)
            val selectedImage = BitmapFactory.decodeStream(imageStream)

            var encodedImage: String? = null
            checkImageDimensions(selectedImage, {
                val base64: String? = ImageUtils.encodeImage(selectedImage)
                encodedImage = base64
            }, {
                // Show toast
                LifecycleUtils.showToast(activity, "Image too large")
               encodedImage = null
            }, {
                val resized = resizeImage(selectedImage)
                val base64: String? = encodeImage(resized)
                encodedImage = base64
            })

            return encodedImage
        }

        // for the received images
        fun base64ToBitmap(encodedImage: String): Bitmap{
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

        fun setProfilePic(base64: String, view: ImageView, viewParent: FrameLayout, activity: SignedinActivity){
            if(base64.isNotEmpty()){
                val bitmap = base64ToBitmap(base64)

                Glide.with(activity).load(bitmap).dontAnimate().into(view)
//                view.setImageBitmap(bitmap)
                viewParent.visibility = View.VISIBLE
            }
        }

        fun hideViewParent(viewParent: FrameLayout){
            viewParent.visibility = View.GONE
        }

        fun setProfilePic_fullObject(profileImage: ProfileImage, view: ImageView, viewParent: FrameLayout){
            if(profileImage.image?.isNotEmpty() == true){
                val bitmap = base64ToBitmap(profileImage.image!!)

                view.setImageBitmap(bitmap)
                viewParent.visibility = View.VISIBLE
            }
        }

        fun setProfilePicOtherUser(user: User, view: ImageView, viewParent: FrameLayout, activity: SignedinActivity){
            activity.determineOtherImgFetchMethod(user, {
                if (it != null) {
                    setProfilePic(it, view, viewParent, activity)
                }
            }, {
                if (it != null) {
                    setProfilePic(it, view, viewParent, activity)
                }
            })
        }

        fun setProfilePicOtherUser(user: User, view: ImageView, viewParent: FrameLayout, activity: SignedinActivity, imgCallback: (image: String?)-> Unit){
            activity.determineOtherImgFetchMethod(user, {
                if (it != null) {
                    imgCallback(it)
                    setProfilePic(it, view, viewParent, activity)
                } else {
                    imgCallback(null)
                }
            }, {
                if (it != null) {
                    imgCallback(it)
                    setProfilePic(it, view, viewParent, activity)
                } else {
                    imgCallback(null)
                }
            })
        }

        fun setProfilePicOtherUser_fullObject(user: User, view: ImageView, viewParent: FrameLayout, activity: SignedinActivity, imgCallback: (image: ProfileImage?)-> Unit){
            activity.determineOtherImgFetchMethod_fullObject(user, {
                if (it?.image != null) {
                    imgCallback(it)
                    setProfilePic(it.image!!, view, viewParent, activity)
                } else {
                    imgCallback(null)
                    hideViewParent(viewParent)
                }
            }, {
                if (it?.image != null) {
                    imgCallback(it)
                    setProfilePic(it.image!!, view, viewParent, activity)
                } else {
                    imgCallback(null)
                    hideViewParent(viewParent)
                }
            })
        }

        fun setProfilePicGroup(group: GroupRoom, view: ImageView, viewParent: FrameLayout, activity: SignedinActivity, imgCallback: (image: String?)-> Unit){
            activity.determineGroupFetchMethod(group, {
                if (it != null) {
                    imgCallback(it)
                    setProfilePic(it, view, viewParent, activity)
                } else {
                    imgCallback(null)
                }
            }, {
                if (it != null) {
                    imgCallback(it)
                    setProfilePic(it, view, viewParent, activity)
                } else {
                    imgCallback(null)
                }
            })
        }

        fun setProfilePicGroup_fullObject(group: GroupRoom, view: ImageView, viewParent: FrameLayout, activity: SignedinActivity, imgCallback: (image: ProfileImage?)-> Unit){
            activity.determineGroupFetchMethod_fullObject(group, {
                if (it != null) {
                    imgCallback(it)
                    if(it.image != null){
                        setProfilePic(it.image!!, view, viewParent, activity)
                    }
                } else {
                    imgCallback(null)
                    hideViewParent(viewParent)
                }
            }, {
                if (it != null) {
                    imgCallback(it)
                    if(it.image != null){
                        setProfilePic(it.image!!, view, viewParent, activity)
                    }
                } else {
                    imgCallback(null)
                    hideViewParent(viewParent)
                }
            })
        }


        // TODO: Write algorithm to resize image
//        fun resizeImage(image: Bitmap): Bitmap {
//            // return image if less than 1 mb regardless of the resolution
//            Log.d("LLL", "Image compress ran")
//            if (image.byteCount <= 1000000){
//                return image
//            }
//
//        }

        fun resizeImage(image: Bitmap): Bitmap {

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

        fun setChatImage(base64: String?, image: ImageView, viewParent: FrameLayout, activity: SignedinActivity){
            if(base64?.isNotEmpty() == true){
                val bitmap = base64ToBitmap(base64)
                Glide.with(activity).load(bitmap).dontAnimate().into(image)
                viewParent.visibility = View.VISIBLE

//                image.setImageBitmap(bitmap)
            }
        }

        fun getAndSetChatImage(message: Message, chatRoomId: String, image: ImageView, viewParent: FrameLayout, activity: SignedinActivity){
            activity.determineMessageImgFetchMethod(message, chatRoomId, {
                if(it != null){
                    setChatImage(it, image, viewParent, activity)
                }
            }, {
                if(it != null){
                    setChatImage(it,  image, viewParent, activity)
                }
            })
        }

        fun getAndSetChatImage(message: Message, chatRoomId: String, image: ImageView, viewParent: FrameLayout, activity: SignedinActivity, imgCallback: (image: String)-> Unit){
            activity.determineMessageImgFetchMethod(message, chatRoomId,{
                if(it != null){
                    setChatImage(it, image, viewParent, activity)
                    imgCallback(it)
                }
            }, {
                if(it != null){
                    setChatImage(it,  image, viewParent, activity)
                    imgCallback(it)
                }
            })
        }

        /*
            First checks the DB if there is an existing chat image
            if: It sets the image from the DB in the image view
            else: It fetches the image from firebase and sets it in the image view
        */
        fun getAndSetChatImage_fullObject(message: Message, chatRoomId: String, imageView: ImageView, viewParent: FrameLayout, activity: SignedinActivity, imgCallback: (image: Image)-> Unit){
            activity.determineMessageImgFetchMethod_fullObject(message, chatRoomId) {
                if(it != null){
                    setChatImage(it.image, imageView, viewParent, activity)
                    imgCallback(it)
                }
            }
        }

        fun check_if_chat_image_in_db(message: Message, activity: SignedinActivity, imageCallback: (image: Image?) -> Unit){
            activity.checkIfImgMessageInDb(message) {
                if(it != null){
                    imageCallback(it)
                } else {
                    imageCallback(null)
                }
            }
        }

        fun displayImageMessage(image: Image, message: Message, activity: SignedinActivity){
            val otherUser = activity.firebaseViewModel.selectedChatRoomUser.value
            val currentUserId = activity.firebaseAuth.currentUser!!.uid

            if(otherUser != null){
                var imageOwnerFormat = ""

                if(image.ownerId.equals(currentUserId)){
                    imageOwnerFormat = activity.getString(R.string.from_you)
                } else {
                    imageOwnerFormat = activity.getString(R.string.from_user, otherUser.name)
                }

                if (image.image != null){
                    activity.imageViewModel.setOverlayProps(
                        imageOwnerFormat,
                        activity.getString(R.string.chat_image),
                        message.time,
                        image.image!!
                    )
                }


                activity.imageViewModel.setShowProfileImage(true)
            }
        }

        fun displayImageMessage_group(image: Image, message: Message, activity: SignedinActivity){
            val currentUserId = activity.firebaseAuth.currentUser!!.uid
            val selectedGroupRoomParticipants = activity.firebaseViewModel.selectedGroupParticipants.value

            if(selectedGroupRoomParticipants != null){
                var selectedUser: User? = null
                for(i in selectedGroupRoomParticipants){
                    if(i.userUID == image.ownerId){
                        selectedUser = i
                    }
                }
                if(selectedUser != null){
                    var imageOwnerFormat = ""

                    if(image.ownerId.equals(currentUserId)){
                        imageOwnerFormat = activity.getString(R.string.from_you)
                    } else {
                        imageOwnerFormat = activity.getString(R.string.from_user, selectedUser.name)
                    }

                    if(image.image != null){
                        activity.imageViewModel.setOverlayProps(
                            imageOwnerFormat,
                            activity.getString(R.string.chat_image),
                            message.time,
                            image.image!!
                        )
                    }

                    activity.imageViewModel.setShowProfileImage(true)
                }
            }
        }


        fun displayProfilePicture(image: ProfileImage, user: User, activity: SignedinActivity){
            val currentUserId = activity.firebaseViewModel.currentUser.value

            activity.hideKeyboard()

            if(image.image != null){
                var imageOwnerFormat = ""

                if(image.ownerId.equals(currentUserId)){
                    imageOwnerFormat = activity.getString(R.string.you)
                } else {
                    imageOwnerFormat = user.name
                }

                activity.imageViewModel.setOverlayProps(
                    imageOwnerFormat,
                    activity.getString(R.string.profile_image),
                    image.imgChangeTimestamp,
                    image.image!!
                )

                activity.imageViewModel.setShowProfileImage(true)
            }
        }

        fun displayGroupImage(image: ProfileImage, group: GroupRoom, activity: SignedinActivity){
            activity.hideKeyboard()

            if(image.image != null){
                var imageOwnerFormat = group.groupName.toString()

                activity.imageViewModel.setOverlayProps(
                    imageOwnerFormat,
                    activity.getString(R.string.group_image),
                    image.imgChangeTimestamp,
                    image.image!!
                )

                activity.imageViewModel.setShowProfileImage(true)
            }
        }

        fun displayPublicPostImage(post: PublicPost, user: User, activity: SignedinActivity){
            if(post.image != null){
                var imageOwnerFormat = user.name

                activity.imageViewModel.setOverlayProps(
                    imageOwnerFormat,
                    activity.getString(R.string.public_post_image),
                    post.postTimestamp,
                    post.image!!
                )

                activity.imageViewModel.setShowProfileImage(true)
            }
        }


        fun uploadChatImage(uri: Uri, chatRoomId: String, activity: SignedinActivity, success: (message: Message, image: Image)-> Unit){
            val encoded = encodeUri(uri, activity)
            val imageId = MessageUtils.generateUID(30)

            if(encoded != null){

                // TODO: Change owner id from current user to current chat room
                val image = Image(imageId, activity.firebaseAuth.currentUser!!.uid)
                val message = Message(MessageUtils.generateUID(30), imageId, System.currentTimeMillis(), activity.firebaseAuth.currentUser!!.uid, MessageType.IMAGE)

                activity.firebaseViewModel.uploadChatImage(image, chatRoomId, encoded, activity.firebaseStorage, activity.mDbRef, {
                    success(message, it)
                }, {})
            }
        }


    }
}