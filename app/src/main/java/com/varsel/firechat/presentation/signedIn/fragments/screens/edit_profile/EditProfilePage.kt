package com.varsel.firechat.presentation.signedIn.fragments.screens.edit_profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.varsel.firechat.domain.use_case.camera.OpenCamera_UseCase
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.current_user.EditUserFields
import com.varsel.firechat.domain.use_case.image.EncodeUri_UseCase
import com.varsel.firechat.domain.use_case.image.HandleOnActivityResult_image_UseCase
import com.varsel.firechat.domain.use_case.image.OpenImagePicker_UseCase
import com.varsel.firechat.domain.use_case.profile_image.DisplayProfileImage
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.common._utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import com.varsel.firechat.domain.use_case._util.InfobarColors
import com.varsel.firechat.domain.use_case._util.status_bar.ChangeStatusBarColor_UseCase
import com.varsel.firechat.domain.use_case.profile_image.GetCurrentUserProfileImageUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

    @Inject
    lateinit var openCamera: OpenCamera_UseCase

    @Inject
    lateinit var openImagePicker: OpenImagePicker_UseCase

    @Inject
    lateinit var handleOnActivityResult: HandleOnActivityResult_image_UseCase

    @Inject
    lateinit var encodeUri: EncodeUri_UseCase

    @Inject
    lateinit var gdtCurrentUserImage: GetCurrentUserProfileImageUseCase

    @Inject
    lateinit var changeStatusBarColor: ChangeStatusBarColor_UseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfilePageBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)

        parent = activity as SignedinActivity
        changeStatusBarColor(R.color.light_blue, false, parent)

        collectState()

        binding.backButton.setOnClickListener {
            popNavigation()
        }

        return view
    }

    private fun collectState() {
        collectLatestLifecycleFlow(viewModel.state) {
            setProfileImage(it.profileImage)

        }
        collectLatestLifecycleFlow(viewModel.user) {
            if(it != null) {
                setBindings(it)

                binding.actionSheetClickable.setOnClickListener { it2 ->
                    checkServerConnection().onEach { isConnected ->
                        if(isConnected) {
                            openEditProfileActionsheet(it)
                        }
                    }.launchIn(lifecycleScope)
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

        handleOnActivityResult(requestCode, resultCode, data, {
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
        val base64: String? = encodeUri(uri, parent)
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
            lifecycleScope.launch {
                collectLatestLifecycleFlow(viewModel.state) {
                    Log.d("CLEAN", "Expand clicked")
                    if(it.profileImage?.image != null){
                        displayProfileImage(it.profileImage, currentUser, parent)
                        dialog.dismiss()
                    }
                }

            }
        }

        dialogBinding.pickImage.setOnClickListener {
            openImagePicker(this)
            dialog.dismiss()
        }

        dialogBinding.openCamera.setOnClickListener {
            openCamera(requireContext(), this, parent)
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
        viewModel.editUser(EditUserFields.NAME, dialogBinding.nameEditText.text.toString(), parent)
        viewModel.editUser(EditUserFields.ABOUT, dialogBinding.aboutEditText.text.toString(), parent)
        viewModel.editUser(EditUserFields.OCCUPATION, dialogBinding.occupationEditText.text.toString(), parent)
        viewModel.editUser(EditUserFields.LOCATION, dialogBinding.locationEditText.text.toString(), parent)

        if(dialogBinding.phoneEditText.text.length == 0) {
            viewModel.editUser(EditUserFields.PHONE, "", parent)
        } else if (dialogBinding.phoneEditText.text.length in 9..19){
            viewModel.editUser(EditUserFields.PHONE, dialogBinding.phoneEditText.text.toString(), parent)
        } else {
            parent.infobarController.showBottomInfobar(getString(R.string.invalid_phone_number_length), InfobarColors.FAILURE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}