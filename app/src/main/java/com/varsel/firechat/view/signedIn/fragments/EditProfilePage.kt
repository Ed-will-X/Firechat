package com.varsel.firechat.view.signedIn.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActionSheetEditProfileBinding
import com.varsel.firechat.databinding.ActionSheetProfileImageBinding
import com.varsel.firechat.databinding.FragmentEditProfilePageBinding
import com.varsel.firechat.model.ProfileImage.ProfileImage
import com.varsel.firechat.utils.AnimationUtils
import com.varsel.firechat.utils.ExtensionFunctions.Companion.observeOnce
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.viewModel.FirebaseViewModel

private val REQUEST_TAKE_PHOTO = 0
private val REQUEST_SELECT_IMAGE_IN_ALBUM = 1

class EditProfilePage : Fragment() {
    private var _binding: FragmentEditProfilePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfilePageBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity

        setBindings()

        parent.profileImageViewModel.profileImageEncoded.observe(viewLifecycleOwner, Observer {
            if(it != null){
                ImageUtils.setProfilePic(it, binding.profileImage, binding.profileImageParent)
            }
        })

        binding.actionSheetClickable.setOnClickListener {
            openEditProfileActionsheet()
        }

        return view
    }

    private fun openImagePicker(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_SELECT_IMAGE_IN_ALBUM)
    }

    private fun openCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if(isCameraPermissionGranted()){
            startActivityForResult(intent, REQUEST_TAKE_PHOTO)
        } else {
            requestCameraPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM && resultCode == Activity.RESULT_OK){

            var uri: Uri? = data?.data
            if(uri != null) {

                uploadImage(uri) {
//                    binding.profileImage.setImageURI(uri)
//                    binding.profileImageParent.visibility = View.VISIBLE
                }
            }
        }
        else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK){
            val image: Bitmap = data?.extras?.get("data") as Bitmap

            if(image != null) {
                val base64: String? = ImageUtils.encodeImage(image)
                uploadImage(base64!!) {
//                    binding.profileImage.setImageBitmap(image)
//                    binding.profileImageParent.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun isCameraPermissionGranted(): Boolean{
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(
            parent,
            arrayOf<String>(Manifest.permission.CAMERA),
            1
        )
    }

    private fun uploadImage(uri: Uri, successCallback: ()-> Unit){
        val base64: String? = ImageUtils.uriToBitmap(uri, parent)
        val currentUser = parent.firebaseAuth.currentUser!!.uid
        val timestamp = System.currentTimeMillis()
        val profileImage =
            ProfileImage(currentUser, base64!!, timestamp)

        parent.firebaseViewModel.uploadProfileImage(profileImage, parent.mDbRef, parent.firebaseAuth, {
            parent.firebaseViewModel.appendProfileImageTimestamp(parent.firebaseAuth, parent.mDbRef, timestamp, {
                parent.profileImageViewModel.storeImage(profileImage)
                parent.profileImageViewModel.profileImageEncoded.value = profileImage.image
                successCallback()
            }, {})
        }, {
            Toast.makeText(requireContext(), getString(R.string.image_upload_error), Toast.LENGTH_SHORT).show()
        })
    }

    private fun uploadImage(base64: String, successCallback: () -> Unit){
        val currentUser = parent.firebaseAuth.currentUser!!.uid
        val timestamp = System.currentTimeMillis()
        val profileImage =
            ProfileImage( currentUser, base64, timestamp)

        parent.firebaseViewModel.uploadProfileImage(profileImage, parent.mDbRef, parent.firebaseAuth, {
            parent.firebaseViewModel.appendProfileImageTimestamp(parent.firebaseAuth, parent.mDbRef, timestamp, {
                parent.profileImageViewModel.storeImage(profileImage)
                parent.profileImageViewModel.profileImageEncoded.value = profileImage.image
                successCallback()
            }, {})
        }, {
            Toast.makeText(requireContext(), getString(R.string.image_upload_error), Toast.LENGTH_SHORT).show()
        })
    }

    private fun removeImage(successCallback: () -> Unit){
        val currentUserId = parent.firebaseAuth.currentUser!!.uid
        val timestamp = System.currentTimeMillis()
        parent.firebaseViewModel.removeProfileImage(parent.mDbRef, parent.firebaseAuth, {
            parent.firebaseViewModel.appendProfileImageTimestamp(parent.firebaseAuth, parent.mDbRef, timestamp, {
                val image = parent.profileImageViewModel.getImageById(currentUserId)

                image.observeOnce(viewLifecycleOwner, Observer {
                    if(it != null){
                        parent.profileImageViewModel.deleteImage(it)
                        parent.profileImageViewModel.profileImageEncoded.value = null
                    }
                })
                successCallback()
            }, {})
//            parent.settingViewModel.deleteProfilePic()
        }, {})
    }

    private fun openEditProfileActionsheet(){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetEditProfileBinding.inflate(layoutInflater, this.binding.root, false)
        val view = dialogBinding.root

        setActionsheetBindings(dialogBinding)
        dialog.setContentView(view)

        dialogBinding.editProfileBtn.setOnClickListener {
            editUser(dialogBinding)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setBindings(){
        parent.firebaseViewModel.currentUser.observe(viewLifecycleOwner, Observer {
            binding.emailText.text = it?.email ?: "-"
            binding.nameText.text = it?.name ?: "-"
            binding.locationText.text = it?.location ?: "-"
            binding.phoneText.text = it?.phone ?: "-"
            binding.occupationText.text = it?.occupation ?: "-"

            binding.imageIcon.setOnClickListener {
                showImageOptionsActionsheet()
            }

            binding.profileImageSilhouette.setOnClickListener {
                showImageOptionsActionsheet()
            }

//            setProfilePic()

//            ImageUtils.setProfileImage(it?.profileImage, binding.profileImageParent, binding.profileImage)
        })
    }

    private fun showImageOptionsActionsheet(){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetProfileImageBinding.inflate(layoutInflater, this.binding.root, false)
        val view = dialogBinding.root


        dialog.setContentView(view)

        dialogBinding.openCamera.setOnClickListener {

        }

        dialogBinding.pickImage.setOnClickListener {
            openImagePicker()
        }

        dialogBinding.openCamera.setOnClickListener {
            openCamera()
        }

        dialogBinding.removeImage.setOnClickListener {
            removeImage {
                binding.profileImage.setImageURI(null)
                binding.profileImageParent.visibility = View.GONE
            }
        }

        AnimationUtils.changeDialogDimAmount(dialog, 0f)
        dialog.show()
    }

    private fun setActionsheetBindings(dialogBinding: ActionSheetEditProfileBinding){
        val firebaseViewModel: FirebaseViewModel = parent.firebaseViewModel

        dialogBinding.nameEditText.setText(firebaseViewModel.currentUser.value?.name)

        if(firebaseViewModel.currentUser.value?.phone == null){
            dialogBinding.phoneEditText.setText("+")
        } else {
            dialogBinding.phoneEditText.setText(firebaseViewModel.currentUser.value?.phone)
        }

        dialogBinding.occupationEditText.setText(firebaseViewModel.currentUser.value?.occupation)
        dialogBinding.locationEditText.setText(firebaseViewModel.currentUser.value?.location)
        dialogBinding.aboutEditText.setText(firebaseViewModel.currentUser.value?.about)
    }

    private fun editUser(dialogBinding: ActionSheetEditProfileBinding){
        parent.firebaseViewModel.editUser("name",dialogBinding.nameEditText.text.toString(), parent.firebaseAuth, parent.mDbRef)
        parent.firebaseViewModel.editUser("about",dialogBinding.aboutEditText.text.toString(), parent.firebaseAuth, parent.mDbRef)

        if(binding.phoneText.text.length in 8..14){
            parent.firebaseViewModel.editUser("phone",dialogBinding.phoneEditText.text.toString(), parent.firebaseAuth, parent.mDbRef)
        }
        parent.firebaseViewModel.editUser("occupation",dialogBinding.occupationEditText.text.toString(), parent.firebaseAuth, parent.mDbRef)
        parent.firebaseViewModel.editUser("location",dialogBinding.locationEditText.text.toString(), parent.firebaseAuth, parent.mDbRef)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}