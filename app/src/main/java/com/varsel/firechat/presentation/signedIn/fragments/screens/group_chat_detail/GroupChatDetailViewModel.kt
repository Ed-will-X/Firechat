package com.varsel.firechat.presentation.signedIn.fragments.screens.group_chat_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.domain.use_case.profile_image.GetOtherUserProfileImageUseCase
import com.varsel.firechat.domain.use_case.profile_image.SetProfilePicUseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupChatDetailViewModel @Inject constructor(
    val setProfilePicUseCase: SetProfilePicUseCase,
    val getOtherUserProfileImageUseCase: GetOtherUserProfileImageUseCase
): ViewModel() {
    val actionSheetUserId = MutableLiveData<String>()
    var prevParticipantSize = MutableLiveData<Int>()
    var prevAdminSize = MutableLiveData<Int>()
    var prevGroupId = MutableLiveData<String>()

    fun getParticipants(parent: SignedinActivity){
        var users = mutableListOf<User>()
        for (i in parent.firebaseViewModel.selectedGroupRoom.value!!.participants!!.values.toList()){
            parent.firebaseViewModel.getUserSingle(i, parent.mDbRef, {
                if (it != null) {
                    users.add(it)
                }
            }, {
                parent.firebaseViewModel.selectedGroupParticipants.value = users
            })
        }
    }

    fun getNonParticipants(parent: SignedinActivity){
        val participants: List<String> = parent.firebaseViewModel.selectedGroupRoom.value!!.participants?.values!!.toList()
        val friends: List<User>? = parent.firebaseViewModel.friends.value?.toList()
        val non_participants = mutableListOf<User>()

        if(friends != null){
            for((i_index, i_value) in friends.withIndex()){
                for((j_index, j_value) in participants.withIndex()){
                    if(i_value.userUID == j_value){
                        break
                    } else if(j_index == participants.size -1) {
                        non_participants.add(i_value)
                    }
                }
            }
        }

        parent.firebaseViewModel.selectedGroup_nonParticipants.value = non_participants
    }

    fun determineGetParticipants(groupRoom: GroupRoom, parent: SignedinActivity){
        if(prevGroupId.value == groupRoom.roomUID){
            if(groupRoom.admins?.size != prevAdminSize.value){
                getParticipants(parent)
            }

            if(groupRoom.participants?.size != prevParticipantSize.value){
                getParticipants(parent)
            }
        } else {
            getParticipants(parent)
        }


        prevParticipantSize.value = groupRoom.participants?.size
        prevAdminSize.value = groupRoom.admins?.size
        prevGroupId.value = groupRoom.roomUID
    }
}