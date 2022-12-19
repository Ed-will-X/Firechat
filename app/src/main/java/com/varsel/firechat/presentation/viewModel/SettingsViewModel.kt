package com.varsel.firechat.presentation.viewModel

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.varsel.firechat.presentation.signedOut.SignedoutActivity
import kotlinx.coroutines.flow.first

class DatastoreKeys {
    companion object {
        val OS_THEME = preferencesKey<Boolean>("OS_THEME")
        val DARK_MODE = preferencesKey<Boolean>("DARK_MODE")
    }
}

class SettingsViewModel: ViewModel() {

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

    fun logout(activity: FragmentActivity, context: Context?, callback: ()-> Unit){
        val intent = Intent(context, SignedoutActivity::class.java)
        callback()

        activity.startActivity(intent)
        activity.finish()

        // run firebase logout
    }
}