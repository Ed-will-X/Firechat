package com.varsel.firechat.presentation.signedIn.fragments.screens.group_chat_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.R
import com.varsel.firechat.common.Response
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.ProfileImage.ProfileImage
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case._util.InfobarColors
import com.varsel.firechat.domain.use_case._util.animation.ChangeDialogDimAmountUseCase
import com.varsel.firechat.domain.use_case.current_user.GetCurrentUserIdUseCase
import com.varsel.firechat.domain.use_case.group_chat.*
import com.varsel.firechat.domain.use_case.message.GetGroupRoomsRecurrentUseCase
import com.varsel.firechat.domain.use_case.profile_image.*
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GroupChatDetailViewModel @Inject constructor(
    val setProfilePicUseCase: SetProfilePicUseCase,
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase,
    val changeDialogDimAmountUseCase: ChangeDialogDimAmountUseCase,
    private val getAllGroups: GetGroupRoomsRecurrentUseCase,
    val getGroupParticipantsUseCase: GetGroupParticipantsUseCase,
    val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    val makeAdminUseCase: MakeAdminUseCase,
    val removeAdminUseCase: RemoveAdminUseCase,
    val removefromgroupUsecase: RemoveFromGroup_UseCase,
    val exitgroupUsecase: ExitGroup_UseCase,
    val editGroupUseCase: EditGroupUseCase,
    val removegroupimageUsecase: RemoveGroupImage_UseCase,
    val uploadGroupImageUseCase: UploadGroupImageUseCase,
    val getGroupImage_UseCase: GetGroupImageUseCase
    ): ViewModel() {
    val actionSheetUserId = MutableLiveData<String>()
    var prevParticipantSize = MutableLiveData<Int>()
    var prevAdminSize = MutableLiveData<Int>()
    var prevGroupId = MutableLiveData<String>()

    private val _state = MutableLiveData(GroupChatDetailState())
    val state = _state

    val _hasRun = MutableLiveData(false)

    private val _participants = MutableLiveData(listOf<User>())
    val participsnts = _participants

    init {
        getGroupRooms()
    }
    private fun getGroupRooms() {
        val groupRooms = getAllGroups().value.data
        _state.value = _state.value?.copy(groupRooms = groupRooms ?: listOf())
    }

    fun getGroupChat(id: String) {
        for (i in state.value?.groupRooms ?: listOf()) {
            if(i.roomUID == id) {
                // Sets the selected room value
                _state.value = _state.value?.copy(selectedGroup = i)

                // gets the participants
                getParticipants(id, i)
                break
            }
        }
    }

    fun getGroupImage(groupRoom: GroupRoom) {
        viewModelScope.launch {
            getGroupImage_UseCase(groupRoom).onEach {
                _state.value = _state.value?.copy(groupImage = it)
            }.launchIn(this)
        }
    }

    fun uploadGroupImage(roomId: String, profileImage: ProfileImage, base64: String, parent: SignedinActivity) {
        viewModelScope.launch {
            parent.infobarController.showBottomInfobar(parent.getString(R.string.uploading_group_image), InfobarColors.UPLOADING)

            uploadGroupImageUseCase(roomId, profileImage, base64).onEach {
                when(it) {
                    is Response.Success -> {
                        _state.value = _state.value?.copy(groupImage = ProfileImage(profileImage, base64))
                        if(state.value?.selectedGroup != null) {
                            getGroupImage(state.value?.selectedGroup!!)
                        }
                        parent.infobarController.showBottomInfobar(parent.getString(R.string.group_image_upload_successful), InfobarColors.SUCCESS)
                    }
                    is Response.Loading -> {

                    }
                    is Response.Fail -> {
                        parent.infobarController.showBottomInfobar(parent.getString(R.string.group_image_upload_error), InfobarColors.FAILURE)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun removeGroupImage(roomId: String, activity: SignedinActivity) {
        viewModelScope.launch {
            activity.infobarController.showBottomInfobar(activity.getString(R.string.removing_group_image), InfobarColors.UPLOADING)

            removegroupimageUsecase(roomId).onEach {
                when(it) {
                    is Response.Success -> {
                        _state.value = _state.value?.copy(groupImage = null)
//                        if(state.value?.selectedGroup != null) {
//                            getGroupImage(state.value?.selectedGroup!!)
//                        }
                        activity.infobarController.showBottomInfobar(activity.getString(R.string.remove_group_image_successful), InfobarColors.SUCCESS)
                    }
                    is Response.Loading -> {

                    }
                    is Response.Fail -> {
                        activity.infobarController.showBottomInfobar(activity.getString(R.string.remove_group_image_error), InfobarColors.FAILURE)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    // TODO: Delete
//    fun getNonParticipants(parent: SignedinActivity){
//        val participants: List<String> = parent.firebaseViewModel.selectedGroupRoom.value!!.participants?.values!!.toList()
//        val friends: List<User>? = parent.firebaseViewModel.friends.value?.toList()
//        val non_participants = mutableListOf<User>()
//
//        if(friends != null){
//            for((i_index, i_value) in friends.withIndex()){
//                for((j_index, j_value) in participants.withIndex()){
//                    if(i_value.userUID == j_value){
//                        break
//                    } else if(j_index == participants.size -1) {
//                        non_participants.add(i_value)
//                    }
//                }
//            }
//        }
//
//    }


    fun getParticipants(id: String, group: GroupRoom) {
        getGroupParticipantsUseCase(id).onEach {
            if(group.participants.count() == it.count()) {
                _participants.value = it
            }
        }.launchIn(viewModelScope)
    }
}