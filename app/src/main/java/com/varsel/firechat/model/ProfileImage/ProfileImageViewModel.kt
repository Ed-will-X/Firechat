package com.varsel.firechat.model.ProfileImage

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.firechat.model.User.User
import kotlinx.coroutines.launch
import kotlin.concurrent.fixedRateTimer

class ProfileImageViewModel(val dao: ProfileImageDao): ViewModel() {
    val profileImage = MutableLiveData<ProfileImage>()
    val profileImageEncoded = MutableLiveData<String>()
    val selectedOtherUserProfilePic = MutableLiveData<String>()
    val selectedGroupImageEncoded = MutableLiveData<String>()

    val profileImageFetchBlacklist = MutableLiveData<HashMap<String, Long>>(hashMapOf())

    fun getImageById(id: String): LiveData<ProfileImage> {
        val image = dao.get(id)
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
        val image = dao.get(ownerId)
        return image
    }

    fun setImgFetchCooldown(onEnd: ()-> Unit): CountDownTimer {
        val timer = object : CountDownTimer(4000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                Log.d("LLL", "Ticking")
                TODO("Not yet implemented")
            }

            override fun onFinish() {
                Log.d("LLL", "Finished")
                onEnd()
            }
        }.start()

        return timer
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