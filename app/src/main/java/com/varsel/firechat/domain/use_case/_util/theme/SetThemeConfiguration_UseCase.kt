package com.varsel.firechat.domain.use_case._util.theme

import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.lifecycleScope
import com.varsel.firechat.domain.use_case.settings.GetSetting_Boolean_UseCase
import com.varsel.firechat.presentation.signedIn.fragments.screen_groups.bottomNav.settings.SettingKeys_Boolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetThemeConfiguration_UseCase @Inject constructor(
    val getBoolean: GetSetting_Boolean_UseCase
) {
    operator fun invoke(datastore: DataStore<Preferences>, scope: CoroutineScope) {
        scope.launch {
            val dark_theme = getBoolean(SettingKeys_Boolean.DARK_THEME, datastore)
            val override = getBoolean(SettingKeys_Boolean.OVERRIDE_SYSTEM_THEME, datastore)

            if(override == true) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            } else {
                if(dark_theme == true) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }
}