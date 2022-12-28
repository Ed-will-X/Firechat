package com.varsel.firechat.presentation.signedIn.fragments.screens.edit_profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActionSheetEditProfileBinding
import com.varsel.firechat.databinding.ActionSheetProfileImageBinding
import com.varsel.firechat.databinding.FragmentEditProfilePageBinding
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case.current_user.EditUserFields
import com.varsel.firechat.utils.AnimationUtils
import com.varsel.firechat.utils.ExtensionFunctions.Companion.observeOnce
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.utils.InfobarColors
import com.varsel.firechat.utils.LifecycleUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow

class EditProfilePage : Fragment() {
    private var _binding: FragmentEditProfilePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private val viewModel: EditProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfilePageBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        parent = activity as SignedinActivity
        parent.changeStatusBarColor(R.color.light_blue, false)

        viewModel.getCurrentUser()
        collectState()

        binding.backButton.setOnClickListener {
            popNavigation()
        }

        parent.profileImageViewModel.profileImage_currentUser.observe(viewLifecycleOwner, Observer {
            if(it?.image != null){
                ImageUtils.setProfilePic(it.image!!, binding.profileImage, binding.profileImageParent, parent)
                binding.profileImageParent.visibility = View.VISIBLE

            }
        })


        return view
    }

    private fun collectState() {
        collectLatestLifecycleFlow(viewModel.state) {
            if(it.user != null) {
                setBindings(it.user)

                // TODO: Fix potential memory leak
                LifecycleUtils.observeInternetStatus(parent, this, {
                    binding.actionSheetClickable.setOnClickListener { it2 ->
                        openEditProfileActionsheet(it.user)
                    }
                }, {
                    binding.actionSheetClickable.setOnClickListener(null)
                })

            }
        }
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

            parent.infobarController.showBottomInfobar(parent.getString(R.string.uploading_profile_image), InfobarColors.UPLOADING)

            parent.firebaseViewModel.uploadProfileImage(profileImage, base64, parent.firebaseStorage, parent.mDbRef, currentUser, {
                parent.firebaseViewModel.appendProfileImageTimestamp(parent.firebaseAuth, parent.mDbRef, timestamp, {
                    val profileImage_withBase64 = ProfileImage(profileImage, base64)
                    parent.profileImageViewModel.storeImage(profileImage_withBase64)
                    parent.profileImageViewModel.profileImage_currentUser.value = profileImage_withBase64

                    parent.infobarController.showBottomInfobar(parent.getString(R.string.profile_image_upload_successful), InfobarColors.SUCCESS)

                    successCallback(profileImage)
                }, {
                    parent.infobarController.showBottomInfobar(parent.getString(R.string.group_image_upload_error), InfobarColors.FAILURE)
                })
            }, {
                parent.infobarController.showBottomInfobar(parent.getString(R.string.profile_image_upload_error), InfobarColors.FAILURE)
            })
        }
    }

    // camera
    private fun uploadImage(base64: String, successCallback: () -> Unit){
        val currentUser = parent.firebaseAuth.currentUser!!.uid
        val timestamp = System.currentTimeMillis()
        val profileImage = ProfileImage( currentUser, timestamp)

        parent.infobarController.showBottomInfobar(parent.getString(R.string.uploading_profile_image), InfobarColors.UPLOADING)

        parent.firebaseViewModel.uploadProfileImage(profileImage, base64, parent.firebaseStorage, parent.mDbRef, currentUser, {
            parent.firebaseViewModel.appendProfileImageTimestamp(parent.firebaseAuth, parent.mDbRef, timestamp, {
                val profileImage_withBase64 = ProfileImage(profileImage, base64)

                parent.profileImageViewModel.storeImage(profileImage_withBase64)
                parent.profileImageViewModel.profileImage_currentUser.value = profileImage_withBase64

                parent.infobarController.showBottomInfobar(parent.getString(R.string.profile_image_upload_successful), InfobarColors.SUCCESS)
                successCallback()
            }, {
                parent.infobarController.showBottomInfobar(parent.getString(R.string.group_image_upload_error), InfobarColors.FAILURE)
            })
        }, {
            parent.infobarController.showBottomInfobar(parent.getString(R.string.profile_image_upload_error), InfobarColors.FAILURE)
        })
    }

    private fun removeImage(successCallback: () -> Unit){
        val currentUserId = parent.firebaseAuth.currentUser!!.uid
        val timestamp = System.currentTimeMillis()

        parent.infobarController.showBottomInfobar(parent.getString(R.string.removing_profile_image), InfobarColors.UPLOADING)

        parent.firebaseViewModel.removeProfileImage(parent.mDbRef, parent.firebaseAuth.currentUser!!.uid, parent.firebaseStorage, {
            parent.firebaseViewModel.appendProfileImageTimestamp(parent.firebaseAuth, parent.mDbRef, timestamp, {

                parent.infobarController.showBottomInfobar(parent.getString(R.string.remove_profile_image_successful), InfobarColors.SUCCESS)

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
                parent.infobarController.showBottomInfobar(parent.getString(R.string.remove_profile_image_error), InfobarColors.FAILURE)
            })
        }, {
            parent.infobarController.showBottomInfobar(parent.getString(R.string.remove_profile_image_error), InfobarColors.FAILURE)
        })
    }

    private fun openEditProfileActionsheet(currentUser: User){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetEditProfileBinding.inflate(layoutInflater, this.binding.root, false)
        val view = dialogBinding.root

        val nameEditText = dialogBinding.nameEditText
        val locationEditText = dialogBinding.locationEditText
        val phoneEditText = dialogBinding.phoneEditText
        val occupationEditText = dialogBinding.occupationEditText
        val aboutEditText = dialogBinding.aboutEditText

        setActionsheetBindings(dialogBinding, currentUser)
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

    private fun setBindings(currentUser: User){
        binding.emailText.text = currentUser.email ?: "-"
        binding.nameText.text = currentUser.name ?: "-"
        binding.locationText.text = currentUser.location ?: "-"
        binding.phoneText.text = currentUser.phone ?: "-"
        binding.occupationText.text = currentUser.occupation ?: "-"

        binding.imageIcon.setOnClickListener {
            showImageOptionsActionsheet(currentUser)
        }

        binding.profileImageSilhouette.setOnClickListener {
            showImageOptionsActionsheet(currentUser)
        }

//            setProfilePic()

//            ImageUtils.setProfileImage(it?.profileImage, binding.profileImageParent, binding.profileImage)
    }

    private fun showImageOptionsActionsheet(currentUser: User){
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

    private fun setActionsheetBindings(dialogBinding: ActionSheetEditProfileBinding, currentUser: User){
        dialogBinding.nameEditText.setText(currentUser.name)

        if(currentUser.phone == null){
            dialogBinding.phoneEditText.setText("+")
        } else {
            dialogBinding.phoneEditText.setText(currentUser.phone)
        }

        dialogBinding.occupationEditText.setText(currentUser.occupation)
        dialogBinding.locationEditText.setText(currentUser.location)
        dialogBinding.aboutEditText.setText(currentUser.about)
    }

    private fun editUser(dialogBinding: ActionSheetEditProfileBinding){
        viewModel.editUser(EditUserFields.NAME, dialogBinding.nameEditText.text.toString())
        viewModel.editUser(EditUserFields.ABOUT, dialogBinding.aboutEditText.text.toString())
        viewModel.editUser(EditUserFields.OCCUPATION, dialogBinding.occupationEditText.text.toString())
        viewModel.editUser(EditUserFields.LOCATION, dialogBinding.locationEditText.text.toString())

        if(dialogBinding.phoneEditText.text.length in 9..19){
            viewModel.editUser(EditUserFields.PHONE, dialogBinding.phoneEditText.text.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}