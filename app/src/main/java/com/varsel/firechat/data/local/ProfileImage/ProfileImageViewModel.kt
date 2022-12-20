package com.varsel.firechat.data.local.ProfileImage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.data.local.Chat.GroupRoomEntity
import com.varsel.firechat.data.local.User.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@HiltViewModel
class ProfileImageViewModel @Inject constructor(
    val dao: ProfileImageDao
): ViewModel() {
    val profileImage = MutableLiveData<ProfileImageEntity>()

    // TODO: Switch from encoded strings to the actual ProfileImageObjects
    val profileImage_currentUser = MutableLiveData<ProfileImageEntity>()

    val selectedOtherUserProfilePicChat = MutableLiveData<String>()
    val selectedOtherUserProfilePic = MutableLiveData<String>()

    val selectedGroupImage = MutableLiveData<ProfileImageEntity>()

    val profileImageFetchBlacklist = MutableLiveData<HashMap<String, Long>>(hashMapOf())

    fun nullifyImageInRoom(id: String){
        val profileImage = ProfileImageEntity(id, 0L, null)
        storeImage(profileImage)
    }

    fun getImageById(id: String): LiveData<ProfileImageEntity> {
        val image = dao.get(id)
        return image
    }

    fun storeImage(profileImage: ProfileImageEntity){
        viewModelScope.launch {
            dao.insert(profileImage)
        }
    }

    fun deleteImage(profileImage: ProfileImageEntity){
        viewModelScope.launch {
            dao.delete(profileImage)
        }
    }

    fun checkForProfileImageInRoom(ownerId: String): LiveData<ProfileImageEntity>?{
        val image = dao.get(ownerId)
        return image
    }

    fun isNotUserInBlacklist(user: UserEntity, ifCallback: ()-> Unit, elseCallback: ()-> Unit){
        if(profileImageFetchBlacklist.value?.contains(user.userUID) == false){
            ifCallback()
        } else {
            val userimgTimestampBlacklist = profileImageFetchBlacklist.value?.get(user.userUID)

            if(userimgTimestampBlacklist != user.imgChangeTimestamp){
                ifCallback()
            } else {
                elseCallback()
            }
        }
    }

    fun addUserToBlacklist(user: UserEntity){
        profileImageFetchBlacklist.value?.put(user.userUID, user.imgChangeTimestamp)
    }

    fun isNotGroupInBlacklist(group: GroupRoomEntity, ifCallback: ()-> Unit, elseCallback: ()-> Unit){
        if(profileImageFetchBlacklist.value?.contains(group.roomUID) == false){
            ifCallback()
        } else {
            val userimgTimestampBlacklist = profileImageFetchBlacklist.value?.get(group.roomUID)

            if(userimgTimestampBlacklist != group.imgChangeTimestamp){
                ifCallback()
            } else {
                elseCallback()
            }
        }
    }

    fun addGroupToBlacklist(group: GroupRoomEntity){
        profileImageFetchBlacklist.value?.put(group.roomUID, group.imgChangeTimestamp)
    }

    fun clearBlacklist(){
        profileImageFetchBlacklist.value?.clear()
    }

    fun setClearBlacklistCountdown(){
        fixedRateTimer("blacklist_timer", false, 0L, 1000 * 1000) {
            clearBlacklist()
        }
    }

    fun removeFromBlacklist(userId: String){
        profileImageFetchBlacklist.value?.remove(userId)
    }


}