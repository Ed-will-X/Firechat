package com.varsel.firechat.data.local.ProfileImage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.data.local.Chat.GroupRoom
import com.varsel.firechat.data.local.User.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@HiltViewModel
class ProfileImageViewModel @Inject constructor(
    val dao: ProfileImageDao
): ViewModel() {
    val profileImage = MutableLiveData<ProfileImage>()

    // TODO: Switch from encoded strings to the actual ProfileImageObjects
    val profileImage_currentUser = MutableLiveData<ProfileImage>()

    val selectedOtherUserProfilePicChat = MutableLiveData<String>()
    val selectedOtherUserProfilePic = MutableLiveData<String>() // TODO: Delete this line

    val selectedGroupImage = MutableLiveData<ProfileImage>()

    val profileImageFetchBlacklist = MutableLiveData<HashMap<String, Long>>(hashMapOf())

    fun nullifyImageInRoom(id: String){
        val profileImage = ProfileImage(id, 0L, null)
        storeImage(profileImage)
    }

    fun getImageById(id: String): LiveData<ProfileImage> {
        val image = dao.get_liveData(id)
        return image
    }

    fun storeImage(profileImage: ProfileImage){
        viewModelScope.launch {
            dao.insert(profileImage)
        }
    }

    fun deleteImage(profileImage: ProfileImage){
        viewModelScope.launch {
            dao.delete(profileImage)
        }
    }

    fun checkForProfileImageInRoom(ownerId: String): LiveData<ProfileImage>?{
        val image = dao.get_liveData(ownerId)
        return image
    }

    fun isNotUserInBlacklist(user: User, ifCallback: ()-> Unit, elseCallback: ()-> Unit){
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

    fun addUserToBlacklist(user: User){
        profileImageFetchBlacklist.value?.put(user.userUID, user.imgChangeTimestamp)
    }

    fun isNotGroupInBlacklist(group: GroupRoom, ifCallback: ()-> Unit, elseCallback: ()-> Unit){
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

    fun addGroupToBlacklist(group: GroupRoom){
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