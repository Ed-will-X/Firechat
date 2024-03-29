package com.varsel.firechat.data.local.Setting

import androidx.lifecycle.*
import com.varsel.firechat.data.local.BugReport.BugReport
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val dao: SettingDao
) : ViewModel() {
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

    }

    fun uploadBugReport(bugReportEntity: BugReport, activity: SignedinActivity, successCallback: ()-> Unit, failureCallback: ()-> Unit){

    }
}