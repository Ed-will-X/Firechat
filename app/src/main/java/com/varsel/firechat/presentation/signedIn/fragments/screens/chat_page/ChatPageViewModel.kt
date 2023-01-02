package com.varsel.firechat.presentation.signedIn.fragments.screens.chat_page

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatPageViewModel @Inject constructor(
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase
): ViewModel() {
    val actionBarVisibility = MutableLiveData<Boolean>(false)

    fun toggleActionbarVisibility(){
        actionBarVisibility.value = actionBarVisibility.value?.not()
    }
}