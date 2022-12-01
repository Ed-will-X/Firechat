package com.varsel.firechat.model.Setting

import androidx.lifecycle.*
import com.varsel.firechat.model.BugReport.BugReport
import com.varsel.firechat.view.signedIn.SignedinActivity
import kotlinx.coroutines.launch

class SettingViewModel(private val dao: SettingDao) : ViewModel() {
    val settingConfig = MutableLiveData<Setting>()

    fun storeSetting(setting: Setting){
        viewModelScope.launch {
            dao.insert(setting)
        }
    }

    fun updateSetting(setting: Setting){
        viewModelScope.launch {
            dao.update(setting)
        }
    }

    fun getSetting(userId: String): LiveData<Setting> {
        val configuration = dao.get(userId)
        return configuration
    }

    fun revertSettingsToDefault(setting: Setting){
        val defaults = Setting(setting)

        viewModelScope.launch {
            dao.insert(defaults)
        }
    }

    fun setDarkMode(setting: Setting, value: Boolean){
        setting.setDarkMode(value)
    }

    fun overrideSystemTheme(setting: Setting, value: Boolean){
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

    fun uploadBugReport(bugReport: BugReport, activity: SignedinActivity, successCallback: ()-> Unit, failureCallback: ()-> Unit){
        activity.firebaseViewModel.uploadBugReport(bugReport, activity.mDbRef, activity.firebaseAuth, {
            successCallback()
        }, {
            failureCallback()
        })
    }
}