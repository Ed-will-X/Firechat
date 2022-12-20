package com.varsel.firechat.data.local.Setting

import androidx.lifecycle.*
import com.varsel.firechat.data.local.BugReport.BugReportEntity
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val dao: SettingDao
) : ViewModel() {
    val settingConfig = MutableLiveData<SettingEntity>()

    fun storeSetting(setting: SettingEntity){
        viewModelScope.launch {
            dao.insert(setting)
        }
    }

    fun updateSetting(setting: SettingEntity){
        viewModelScope.launch {
            dao.update(setting)
        }
    }

    fun getSetting(userId: String): LiveData<SettingEntity> {
        val configuration = dao.get(userId)
        return configuration
    }

    fun revertSettingsToDefault(setting: SettingEntity){
        val defaults = SettingEntity(setting)

        viewModelScope.launch {
            dao.insert(defaults)
        }
    }

    fun setDarkMode(setting: SettingEntity, value: Boolean){
        setting.setDarkMode(value)
    }

    fun overrideSystemTheme(setting: SettingEntity, value: Boolean){
        setting.setOverrideSystemTheme(value)
    }


    fun clearAppStorage(){
        // TODO: CLear all room tables

        // TODO: Logout
    }

    fun clearCache(){
        // TODO: Clear firebase cache

        // TODO: Logout to be safe
    }

    fun clearUserSearchHistory(activity: SignedinActivity){
        activity.firebaseViewModel.deleteRecentSearchHistory(activity.firebaseAuth, activity.mDbRef) {

        }
    }

    fun uploadBugReport(bugReportEntity: BugReportEntity, activity: SignedinActivity, successCallback: ()-> Unit, failureCallback: ()-> Unit){
        activity.firebaseViewModel.uploadBugReport(bugReportEntity, activity.mDbRef, activity.firebaseAuth, {
            successCallback()
        }, {
            failureCallback()
        })
    }
}