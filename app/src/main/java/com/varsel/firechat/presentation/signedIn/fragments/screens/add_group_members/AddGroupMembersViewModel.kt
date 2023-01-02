package com.varsel.firechat.presentation.signedIn.fragments.screens.add_group_members

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddGroupMembersViewModel @Inject constructor(
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val setProfilePicUseCase: SetProfilePicUseCase
): ViewModel() {
    val hasBeenClicked = MutableLiveData<Boolean>(false)
}