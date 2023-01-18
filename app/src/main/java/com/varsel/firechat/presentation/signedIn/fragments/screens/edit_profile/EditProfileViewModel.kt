package com.varsel.firechat.presentation.signedIn.fragments.screens.edit_profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.R
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.domain.use_case._util.InfobarColors
import com.varsel.firechat.domain.use_case._util.animation.ChangeDialogDimAmountUseCase
import com.varsel.firechat.domain.use_case.current_user.EditUserUseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetCurrentUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.RemoveProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import com.varsel.firechat.domain.use_case.profile_image.UploadProfileImageUseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    val getCurrentUserRecurrentUseCase: GetCurrentUserRecurrentUseCase,
    val editUserUseCase: EditUserUseCase,
    val getCurrentUserProfileImageUseCase: GetCurrentUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase,
    val uploadProfileImageUseCase: UploadProfileImageUseCase,
    val removeProfileImageUseCase: RemoveProfileImageUseCase,
    val changeDialogDimAmountUseCase: ChangeDialogDimAmountUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(EditProfileState(user = null, isLoading = true))
    val _hasRun = MutableStateFlow(false)
    val state = _state

    init {
        getCurrentUser()
        getProfileImage()
    }

    private fun getProfileImage() {
        viewModelScope.launch {
            getCurrentUserProfileImageUseCase().onEach {
                if(it != null) {
                    _state.value = _state.value.copy(profileImage = it)
                } else {
                    _state.value = _state.value.copy(profileImage = null)
                }
            }.launchIn(this)
        }
    }

    private fun getCurrentUser() {
        getCurrentUserRecurrentUseCase().onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(user = it.data, isLoading = false)
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(user = null, isLoading = true)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(user = null, isLoading = false, errorMessage = it.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun editUser(key: String, value: String) {
        editUserUseCase(key, value).onEach {
            if(it == Response.Fail()) {
                // TODO: Show Error infobar
            }
        }.launchIn(viewModelScope)
    }

    fun uploadImage(profileImage: ProfileImage, base64: String, activity: SignedinActivity) {
        viewModelScope.launch {
            activity.infobarController.showBottomInfobar(activity.getString(R.string.uploading_profile_image), InfobarColors.UPLOADING)

            uploadProfileImageUseCase(profileImage, base64).onEach {
                when(it) {
                    is Response.Success -> {
                        _state.value = _state.value.copy(profileImage = ProfileImage(profileImage, base64))
                        activity.infobarController.showBottomInfobar(activity.getString(R.string.profile_image_upload_successful), InfobarColors.SUCCESS)
                    }
                    is Response.Loading -> {

                    }
                    is Response.Fail -> {
                        activity.infobarController.showBottomInfobar(activity.getString(R.string.profile_image_upload_error), InfobarColors.FAILURE)
                    }
                }
            }.launchIn(this)
        }
    }


    fun removeImage(activity: SignedinActivity) {
        viewModelScope.launch {
            removeProfileImageUseCase().onEach {
                when(it) {
                    is Response.Success -> {
                        _state.value = _state.value.copy(profileImage = null)
                        activity.infobarController.showBottomInfobar(activity.getString(R.string.remove_profile_image_successful), InfobarColors.SUCCESS)
                    }
                    is Response.Loading -> {

                    }
                    is Response.Fail -> {
                        activity.infobarController.showBottomInfobar(activity.getString(R.string.remove_profile_image_error), InfobarColors.FAILURE)
                    }
                }
            }.launchIn(this)
        }
    }
}