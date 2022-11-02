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
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.varsel.firechat.R
import com.varsel.firechat.model.Chat.GroupRoom
import com.varsel.firechat.model.Image.Image
import com.varsel.firechat.model.Message.Message
import com.varsel.firechat.model.Message.MessageType
import com.varsel.firechat.model.ProfileImage.ProfileImage
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.SignedinActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream


val REQUEST_TAKE_PHOTO = 0
val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
class ImageUtils {
    companion object {
        val FHD_width = 1080
        val FHD_height = 1080
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

        private fun checkImageDimensions(image: Bitmap, withinBounds: ()-> Unit, outOfBounds: ()-> Unit, compressedCallback: ()-> Unit){
            // TODO: Add for image upload and experiment for FHD
            Log.d("LLL", "width: ${image.width}")
            Log.d("LLL", "height: ${image.height}")
            if(image.height < FHD_height && image.width < FHD_width){
                Log.d("LLL", "Within bounds")
                withinBounds()
            } else if(image.height > img_limit_height || image.width > img_limit_width) {
                Log.d("LLL", "out of bounds")
                outOfBounds()
            } else {
                Log.d("LLL", "compress callback")
                compressedCallback()
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
//                val resized = resizeImage(selectedImage)
                val base64: String? = encodeImage(selectedImage)
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

        fun setProfilePic(base64: String, view: ImageView, viewParent: View){
            if(base64.isNotEmpty()){
                val bitmap = base64ToBitmap(base64)

                view.setImageBitmap(bitmap)
                viewParent.visibility = View.VISIBLE
            }
        }

        fun setProfilePic_fullObject(profileImage: ProfileImage, view: ImageView, viewParent: View){
            if(profileImage.image?.isNotEmpty() == true){
                val bitmap = base64ToBitmap(profileImage.image!!)

                view.setImageBitmap(bitmap)
                viewParent.visibility = View.VISIBLE
            }
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

        fun setProfilePicOtherUser_fullObject(user: User, view: ImageView, viewParent: View, parent: SignedinActivity, imgCallback: (image: ProfileImage?)-> Unit){
            parent.determineOtherImgFetchMethod_fullObject(user, {
                if (it?.image != null) {
                    imgCallback(it)
                    setProfilePic(it.image!!, view, viewParent)
                } else {
                    imgCallback(null)
                }
            }, {
                if (it?.image != null) {
                    imgCallback(it)
                    setProfilePic(it.image!!, view, viewParent)
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

        fun setProfilePicGroup_fullObject(group: GroupRoom, view: ImageView, viewParent: View, parent: SignedinActivity, imgCallback: (image: ProfileImage?)-> Unit){
            parent.determineGroupFetchMethod_fullObject(group, {
                if (it != null) {
                    imgCallback(it)
                    if(it.image != null){
                        setProfilePic(it.image!!, view, viewParent)
                    }
                } else {
                    imgCallback(null)
                }
            }, {
                if (it != null) {
                    imgCallback(it)
                    if(it.image != null){
                        setProfilePic(it.image!!, view, viewParent)
                    }
                } else {
                    imgCallback(null)
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

        fun setChatImage(base64: String?, image: ImageView){
            if(base64?.isNotEmpty() == true){
                val bitmap = base64ToBitmap(base64)

                image.setImageBitmap(bitmap)
            }
        }

        fun getAndSetChatImage(message: Message, image: ImageView,  activity: SignedinActivity){
            activity.determineMessageImgFetchMethod(message, {
                if(it != null){
                    setChatImage(it, image)
                }
            }, {
                if(it != null){
                    setChatImage(it,  image)
                }
            })
        }

        fun getAndSetChatImage(message: Message, image: ImageView,  activity: SignedinActivity, imgCallback: (image: String)-> Unit){
            activity.determineMessageImgFetchMethod(message, {
                if(it != null){
                    setChatImage(it, image)
                    imgCallback(it)
                }
            }, {
                if(it != null){
                    setChatImage(it,  image)
                    imgCallback(it)
                }
            })
        }

        /*
            First checks the DB if there is an existing chat image
            if: It sets the image from the DB in the image view
            else: It fetches the image from firebase and sets it in the image view
        */
        fun getAndSetChatImage_fullObject(message: Message, imageView: ImageView, activity: SignedinActivity, imgCallback: (image: Image)-> Unit){
            activity.determineMessageImgFetchMethod_fullObject(message) {
                if(it != null){
                    setChatImage(it.image, imageView)
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

        fun fetch_chat_image_from_firebase(message: Message, activity: SignedinActivity, imgCallback: (image: Image) -> Unit){
            Log.d("LLL", "Fetch chat image from firebase called")

            activity.fetchChatImage_fullObject(message.message) {
                if(it != null){
                    Log.d("LLL", "image from firebase not null")
                    imgCallback(it)
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

                activity.imageViewModel.setOverlayProps(
                    imageOwnerFormat,
                    activity.getString(R.string.chat_image),
                    message.time,
                    image.image
                )

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

                    activity.imageViewModel.setOverlayProps(
                        imageOwnerFormat,
                        activity.getString(R.string.chat_image),
                        message.time,
                        image.image
                    )

                    activity.imageViewModel.setShowProfileImage(true)
                }
            }
        }


        fun displayProfilePicture(image: ProfileImage, user: User, activity: SignedinActivity){
            val currentUserId = activity.firebaseViewModel.currentUser.value

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


        fun uploadChatImage(uri: Uri, activity: SignedinActivity, success: (message: Message, image: Image)-> Unit){
            val encoded = encodeUri(uri, activity)
            if(encoded != null){
                val imageId = MessageUtils.generateUID(50)
                // TODO: Change owner id from current user to current chat room
                val image = Image(imageId, activity.firebaseAuth.currentUser!!.uid, encoded)
                val message = Message(MessageUtils.generateUID(50), imageId, System.currentTimeMillis(), activity.firebaseAuth.currentUser!!.uid, MessageType.IMAGE)

                activity.firebaseViewModel.uploadChatImage(image, activity.mDbRef, {
                    success(message, image)
                }, {})
            }
        }
    }
}