package com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.R
import com.varsel.firechat.common.Resource
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.PublicPost.PublicPost
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case._util.InfobarColors
import com.varsel.firechat.domain.use_case.current_user.CheckServerConnectionUseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserRecurrentUseCase
import com.varsel.firechat.domain.use_case.current_user.GetFriendRequests_UseCase
import com.varsel.firechat.domain.use_case.profile_image.GetCurrentUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import com.varsel.firechat.domain.use_case.public_post.UploadPublicPostImageUseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val getCurrentUserRecurrentUseCase: GetCurrentUserRecurrentUseCase,
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase,
    val getCurrentUserProfileImageUseCase: GetCurrentUserProfileImageUseCase,
    val checkServerConnection: CheckServerConnectionUseCase,
    val uploadPublicPostImageUseCase: UploadPublicPostImageUseCase,
    val getfriendrequestsUsecase: GetFriendRequests_UseCase
): ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _friendRequests = MutableStateFlow<List<User>>(listOf())
    val friendRequests: StateFlow<List<User>> = _friendRequests

    init {
        getCurrentUser()
        checkConnection()
    }

    fun checkConnection() {
        checkServerConnection().onEach {
            if(it){
                _state.value = _state.value.copy(isConnectedToServer = true)
            } else {
                _state.value = _state.value.copy(isConnectedToServer = false)
            }
        }.launchIn(viewModelScope)
    }

    fun getProfileImage() {
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

    fun uploadPublicPost(publicPost: PublicPost, base64: String, parent: SignedinActivity) {
        viewModelScope.launch {
            parent.infobarController.showBottomInfobar(parent.getString(R.string.uploading_public_post), InfobarColors.UPLOADING)

            uploadPublicPostImageUseCase(publicPost, base64).onEach {
                when(it) {
                    is Response.Success -> {

                        parent.infobarController.showBottomInfobar(parent.getString(R.string.public_post_upload_successful), InfobarColors.SUCCESS)
                    }
                    is Response.Loading -> {

                    }
                    is Response.Fail -> {
                        parent.infobarController.showBottomInfobar(parent.getString(R.string.chat_image_upload_error), InfobarColors.FAILURE)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun getCurrentUser() {
        getCurrentUserRecurrentUseCase().onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _currentUser.value = it.data
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
//                    _currentUser.value = null
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _currentUser.value = null
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getFriendRequests(requests: HashMap<String, Long>) {
        getfriendrequestsUsecase(requests) {
            _friendRequests.value = it
        }
    }
}