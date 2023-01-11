package com.varsel.firechat.presentation.viewModel

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.varsel.firechat.domain.use_case.current_user.SignoutUseCase
import com.varsel.firechat.presentation.signedOut.SignedoutActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DatastoreKeys {
    companion object {
        val OS_THEME = preferencesKey<Boolean>("OS_THEME")
        val DARK_MODE = preferencesKey<Boolean>("DARK_MODE")
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val signoutUseCase: SignoutUseCase
): ViewModel() {

    suspend fun writeUseOsTheme(value: Boolean, datastore: DataStore<Preferences>){
        datastore.edit {
            it[DatastoreKeys.OS_THEME] = value
        }
    }

    suspend fun readOSTheme(datastore: DataStore<Preferences>): Boolean {
        val preferences = datastore.data.first()

        return preferences[DatastoreKeys.OS_THEME] ?: false
    }

    suspend fun writeDarkMode(value: Boolean, datastore: DataStore<Preferences>){
        datastore.edit {
            it[DatastoreKeys.DARK_MODE] = value
        }
    }

    suspend fun readDarkMode(datastore: DataStore<Preferences>): Boolean{
        val preferences = datastore.data.first()

        return preferences[DatastoreKeys.DARK_MODE] ?: false
    }

    suspend fun clearStorage(){
        // TODO: Clear Chat Images, profile images, public posts

        // TODO: Logout
    }

    suspend fun clearCache(){
        // TODO: Empty firebase cache

        // TODO: Logout
    }

    suspend fun clearUserSearchHistory(){
        // TODO: Empty search history in user object
    }

    suspend fun clearChatSearchHistory(){

    }

    fun logout(activity: FragmentActivity, context: Context?){
        val intent = Intent(context, SignedoutActivity::class.java)

        signoutUseCase()
        activity.startActivity(intent)
        activity.finish()

        // run firebase logout
    }
}