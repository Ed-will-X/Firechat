package com.varsel.firechat.view.signedIn.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActionSheetEditProfileBinding
import com.varsel.firechat.databinding.ActionSheetProfileImageBinding
import com.varsel.firechat.databinding.FragmentEditProfilePageBinding
import com.varsel.firechat.model.ProfileImage.ProfileImage
import com.varsel.firechat.utils.AnimationUtils
import com.varsel.firechat.utils.ExtensionFunctions.Companion.observeOnce
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.InfobarColors
import com.varsel.firechat.utils.LifecycleUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.viewModel.FirebaseViewModel

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

        binding.backButton.setOnClickListener {
            popNavigation()
        }

        parent.profileImageViewModel.profileImage_currentUser.observe(viewLifecycleOwner, Observer {
            if(it?.image != null){
                ImageUtils.setProfilePic(it.image!!, binding.profileImage, binding.profileImageParent, parent)
                binding.profileImageParent.visibility = View.VISIBLE

            }
        })

        LifecycleUtils.observeInternetStatus(parent, this, {
            binding.actionSheetClickable.setOnClickListener {
                openEditProfileActionsheet()
            }
        }, {
            binding.actionSheetClickable.setOnClickListener(null)
        })


        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        ImageUtils.handleOnActivityResult(requireContext(), requestCode, resultCode, data, {
            uploadImage(it) {

            }
        }, {
            if(it != null){
                uploadImage(it) {

                }
            }
        })
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    // gallery
    private fun uploadImage(uri: Uri, successCallback: (profileImage: ProfileImage)-> Unit){
        val base64: String? = ImageUtils.encodeUri(uri, parent)
        if(base64 != null){
            val currentUser = parent.firebaseAuth.currentUser!!.uid
            val timestamp = System.currentTimeMillis()
            val profileImage = ProfileImage(currentUser, timestamp)

            parent.showBottomInfobar(parent.getString(R.string.uploading_profile_image), InfobarColors.UPLOADING)

            parent.firebaseViewModel.uploadProfileImage(profileImage, base64, parent.firebaseStorage, parent.mDbRef, currentUser, {
                parent.firebaseViewModel.appendProfileImageTimestamp(parent.firebaseAuth, parent.mDbRef, timestamp, {
                    val profileImage_withBase64 = ProfileImage(profileImage, base64)
                    parent.profileImageViewModel.storeImage(profileImage_withBase64)
                    parent.profileImageViewModel.profileImage_currentUser.value = profileImage_withBase64

                    parent.showBottomInfobar(parent.getString(R.string.profile_image_upload_successful), InfobarColors.SUCCESS)

                    successCallback(profileImage)
                }, {
                    parent.showBottomInfobar(parent.getString(R.string.group_image_upload_error), InfobarColors.FAILURE)
                })
            }, {
                parent.showBottomInfobar(parent.getString(R.string.profile_image_upload_error), InfobarColors.FAILURE)
            })
        }
    }

    // camera
    private fun uploadImage(base64: String, successCallback: () -> Unit){
        val currentUser = parent.firebaseAuth.currentUser!!.uid
        val timestamp = System.currentTimeMillis()
        val profileImage = ProfileImage( currentUser, timestamp)

        parent.showBottomInfobar(parent.getString(R.string.uploading_profile_image), InfobarColors.UPLOADING)

        parent.firebaseViewModel.uploadProfileImage(profileImage, base64, parent.firebaseStorage, parent.mDbRef, currentUser, {
            parent.firebaseViewModel.appendProfileImageTimestamp(parent.firebaseAuth, parent.mDbRef, timestamp, {
                val profileImage_withBase64 = ProfileImage(profileImage, base64)

                parent.profileImageViewModel.storeImage(profileImage_withBase64)
                parent.profileImageViewModel.profileImage_currentUser.value = profileImage_withBase64

                parent.showBottomInfobar(parent.getString(R.string.profile_image_upload_successful), InfobarColors.SUCCESS)
                successCallback()
            }, {
                parent.showBottomInfobar(parent.getString(R.string.group_image_upload_error), InfobarColors.FAILURE)
            })
        }, {
            parent.showBottomInfobar(parent.getString(R.string.profile_image_upload_error), InfobarColors.FAILURE)
        })
    }

    private fun removeImage(successCallback: () -> Unit){
        val currentUserId = parent.firebaseAuth.currentUser!!.uid
        val timestamp = System.currentTimeMillis()

        parent.showBottomInfobar(parent.getString(R.string.removing_profile_image), InfobarColors.UPLOADING)

        parent.firebaseViewModel.removeProfileImage(parent.mDbRef, parent.firebaseAuth.currentUser!!.uid, parent.firebaseStorage, {
            parent.firebaseViewModel.appendProfileImageTimestamp(parent.firebaseAuth, parent.mDbRef, timestamp, {

                parent.showBottomInfobar(parent.getString(R.string.remove_profile_image_successful), InfobarColors.SUCCESS)

                val image = parent.profileImageViewModel.getImageById(currentUserId)

                image.observeOnce(viewLifecycleOwner, Observer {
                    if(it != null){
                        parent.profileImageViewModel.nullifyImageInRoom(currentUserId)
                        parent.profileImageViewModel.profileImage_currentUser.value = null
//                        parent.profileImageViewModel.profileImageEncodedCurrentUser.value = null
                    }
                })
                successCallback()
            }, {
                parent.showBottomInfobar(parent.getString(R.string.remove_profile_image_error), InfobarColors.FAILURE)
            })
        }, {
            parent.showBottomInfobar(parent.getString(R.string.remove_profile_image_error), InfobarColors.FAILURE)
        })
    }

    private fun openEditProfileActionsheet(){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetEditProfileBinding.inflate(layoutInflater, this.binding.root, false)
        val view = dialogBinding.root

        val nameEditText = dialogBinding.nameEditText
        val locationEditText = dialogBinding.locationEditText
        val phoneEditText = dialogBinding.phoneEditText
        val occupationEditText = dialogBinding.occupationEditText
        val aboutEditText = dialogBinding.aboutEditText

        setActionsheetBindings(dialogBinding)
        dialog.setContentView(view)

        LifecycleUtils.observeInternetStatus(parent, this, {
            dialogBinding.editProfileBtn.isEnabled = true
        }, {
            dialogBinding.editProfileBtn.isEnabled = false
        })

        dialogBinding.editProfileBtn.setOnClickListener {
            nameEditText.setText(nameEditText.text.trim())
            locationEditText.setText(locationEditText.text.trim())
            phoneEditText.setText(phoneEditText.text.trim())
            occupationEditText.setText(occupationEditText.text.trim())
            aboutEditText.setText(aboutEditText.text.trim())

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

        LifecycleUtils.observeInternetStatus(parent, this, {
            dialogBinding.pickImage.isEnabled = true
            dialogBinding.openCamera.isEnabled = true
            dialogBinding.removeImage.isEnabled = true
        }, {
            dialogBinding.pickImage.isEnabled = false
            dialogBinding.openCamera.isEnabled = false
            dialogBinding.removeImage.isEnabled = false
        })

        dialogBinding.expand.setOnClickListener {
            val currentUser = parent.firebaseViewModel.currentUser.value
            val image = parent.profileImageViewModel.profileImage_currentUser.value
            if(currentUser != null && image != null){
                ImageUtils.displayProfilePicture(image, currentUser, parent)
                dialog.dismiss()
            }
        }

        dialogBinding.pickImage.setOnClickListener {
            ImageUtils.openImagePicker(this)
            dialog.dismiss()
        }

        dialogBinding.openCamera.setOnClickListener {
            ImageUtils.openCamera(requireContext(), this, parent)
            dialog.dismiss()
        }

        dialogBinding.removeImage.setOnClickListener {
            dialog.dismiss()
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

        if(dialogBinding.phoneEditText.text.length in 9..19){
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