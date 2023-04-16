package com.varsel.firechat.presentation.signedIn.fragments.screens.about_user

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.R
import com.varsel.firechat.common.Resource
import com.varsel.firechat.databinding.FragmentAboutUserBinding
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case._util.animation.Direction
import com.varsel.firechat.domain.use_case._util.animation.Rotate90UseCase
import com.varsel.firechat.domain.use_case.other_user.GetOtherUserSingle
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboutUserViewModel @Inject constructor(
    val rotate90UseCase: Rotate90UseCase,
    val getOtherUserSingle: GetOtherUserSingle,
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase
): ViewModel() {
    var userDetailsVisible = MutableLiveData<Boolean>(false)

    private val _state = MutableStateFlow(AboutUserState())
    val state: StateFlow<AboutUserState> = _state

    init {

    }

    fun getUser(id: String) {
        getOtherUserSingle(id).onEach {
            when(it) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(selectedUser = it.data)

                    viewModelScope.launch {
                        if(it.data != null) getProfileImage(it.data)
                    }
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(selectedUser = null)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(selectedUser = null)
                }
            }
        }.launchIn(viewModelScope)
    }

    suspend fun getProfileImage(user: User) {
        getOtherUserProfileImageUseCase(user).onEach {
            _state.value = _state.value.copy(profileImage = it)
        }.launchIn(viewModelScope)
    }

    fun setRecyclerViewVisible(binding: FragmentAboutUserBinding){
        userDetailsVisible.value = !userDetailsVisible.value!!
        toggleUserDetailsVisibility(binding)
    }

    private fun toggleUserDetailsVisibility(binding: FragmentAboutUserBinding){
        if(userDetailsVisible.value == true){
            setVisible(binding)
        } else {
            setInvisible(binding)
        }
    }

    fun setVisible(binding: FragmentAboutUserBinding) {
        binding.userDetailsHideable.visibility = View.VISIBLE
        rotate90UseCase(binding.userDetailsIconAnimatable, Direction.Forward())
    }

    fun setInvisible(binding: FragmentAboutUserBinding) {
        binding.userDetailsHideable.visibility = View.GONE
        rotate90UseCase(binding.userDetailsIconAnimatable, Direction.Reverse())
    }
}