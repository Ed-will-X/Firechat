package com.varsel.firechat.presentation.signedIn.fragments.screens.edit_profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActionSheetEditProfileBinding
import com.varsel.firechat.databinding.ActionSheetProfileImageBinding
import com.varsel.firechat.databinding.FragmentEditProfilePageBinding
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.current_user.EditUserFields
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class EditProfilePage : Fragment() {
    private var _binding: FragmentEditProfilePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var viewModel: EditProfileViewModel

    @Inject
    lateinit var displayProfileImage: DisplayProfileImage

    @Inject
    lateinit var checkServerConnection: CheckServerConnectionUseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfilePageBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)

        parent = activity as SignedinActivity
        parent.changeStatusBarColor(R.color.light_blue, false)

        collectState()

        binding.backButton.setOnClickListener {
            popNavigation()
        }

//        parent.profileImageViewModel.profileImage_currentUser.observe(viewLifecycleOwner, Observer {
//            if(it?.image != null){
//                ImageUtils.setProfilePic(it.image!!, binding.profileImage, binding.profileImageParent, parent)
//                binding.profileImageParent.visibility = View.VISIBLE
//
//            }
//        })


        return view
    }

    private fun collectState() {
        collectLatestLifecycleFlow(viewModel.state) {
            if(it.user != null) {
                setBindings(it.user)
                setProfileImage(it.profileImage)

                // TODO: Fix potential memory leak
                if(!viewModel._hasRun.value) {
//                    LifecycleUtils.observeInternetStatus(parent, this, {
//                        binding.actionSheetClickable.setOnClickListener { it2 ->
//                            openEditProfileActionsheet(it.user)
//                        }
//                    }, {
//                        binding.actionSheetClickable.setOnClickListener(null)
//                    })

                    checkServerConnection().onEach { isConnected ->
                        if(isConnected) {
                            binding.actionSheetClickable.setOnClickListener { it2 ->
                                openEditProfileActionsheet(it.user)
                            }
                        } else {
                            binding.actionSheetClickable.setOnClickListener(null)
                        }
                    }.launchIn(lifecycleScope)

                    viewModel._hasRun.value = true
                }

            }
        }
    }

    private fun setProfileImage(profileImage: ProfileImage?){
        if(profileImage != null) {
            viewModel.setProfilePicUseCase(profileImage.image!!, binding.profileImage, binding.profileImageParent, parent)
        } else {
            binding.profileImage.setImageBitmap(null)
            binding.profileImageParent.visibility = View.GONE
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

            viewModel.uploadImage(profileImage, base64, parent)
        }
    }

    // camera
    private fun uploadImage(base64: String, successCallback: () -> Unit){
        val currentUser = parent.firebaseAuth.currentUser!!.uid
        val timestamp = System.currentTimeMillis()
        val profileImage = ProfileImage( currentUser, timestamp)

        viewModel.uploadImage(profileImage, base64, parent)
    }

    private fun removeImage(successCallback: () -> Unit){
        val currentUserId = parent.firebaseAuth.currentUser!!.uid
        val timestamp = System.currentTimeMillis()

        viewModel.removeImage(parent)

//        parent.infobarController.showBottomInfobar(parent.getString(R.string.removing_profile_image), InfobarColors.UPLOADING)

//        parent.firebaseViewModel.removeProfileImage(parent.mDbRef, parent.firebaseAuth.currentUser!!.uid, parent.firebaseStorage, {
//            parent.firebaseViewModel.appendProfileImageTimestamp(parent.firebaseAuth, parent.mDbRef, timestamp, {
//
//                parent.infobarController.showBottomInfobar(parent.getString(R.string.remove_profile_image_successful), InfobarColors.SUCCESS)
//
//                val image = parent.profileImageViewModel.getImageById(currentUserId)
//
//                image.observeOnce(viewLifecycleOwner, Observer {
//                    if(it != null){
//                        parent.profileImageViewModel.nullifyImageInRoom(currentUserId)
//                        parent.profileImageViewModel.profileImage_currentUser.value = null
////                        parent.profileImageViewModel.profileImageEncodedCurrentUser.value = null
//                    }
//                })
//                successCallback()
//            }, {
//                parent.infobarController.showBottomInfobar(parent.getString(R.string.remove_profile_image_error), InfobarColors.FAILURE)
//            })
//        }, {
//            parent.infobarController.showBottomInfobar(parent.getString(R.string.remove_profile_image_error), InfobarColors.FAILURE)
//        })
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

//        LifecycleUtils.observeInternetStatus(parent, this, {
//            dialogBinding.editProfileBtn.isEnabled = true
//        }, {
//            dialogBinding.editProfileBtn.isEnabled = false
//        })

        checkServerConnection().onEach {
            dialogBinding.editProfileBtn.isEnabled = it
        }.launchIn(lifecycleScope)

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
    }

    private fun showImageOptionsActionsheet(currentUser: User){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetProfileImageBinding.inflate(layoutInflater, this.binding.root, false)
        val view = dialogBinding.root


        dialog.setContentView(view)

//        LifecycleUtils.observeInternetStatus(parent, this, {
//            dialogBinding.pickImage.isEnabled = true
//            dialogBinding.openCamera.isEnabled = true
//            dialogBinding.removeImage.isEnabled = true
//        }, {
//            dialogBinding.pickImage.isEnabled = false
//            dialogBinding.openCamera.isEnabled = false
//            dialogBinding.removeImage.isEnabled = false
//        })

        checkServerConnection().onEach {
            if(it) {
                dialogBinding.pickImage.isEnabled = true
                dialogBinding.openCamera.isEnabled = true
                dialogBinding.removeImage.isEnabled = true
            } else {
                dialogBinding.pickImage.isEnabled = false
                dialogBinding.openCamera.isEnabled = false
                dialogBinding.removeImage.isEnabled = false
            }
        }.launchIn(lifecycleScope)

        dialogBinding.expand.setOnClickListener {
            val image = parent.profileImageViewModel.profileImage_currentUser.value
            if(currentUser != null && image != null){
                displayProfileImage(image, currentUser, parent)
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

        viewModel.changeDialogDimAmountUseCase(dialog, 0f)
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