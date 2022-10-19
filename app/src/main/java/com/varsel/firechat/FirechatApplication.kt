package com.varsel.firechat

import android.app.Application
import com.varsel.firechat.model.Image.ProfileImageDatabase
import com.varsel.firechat.model.Setting.SettingDatabase

class FirechatApplication: Application() {
    val settingsDatabase: SettingDatabase by lazy {
        SettingDatabase.getInstance(this)
    }

    val profileImageDatabase: ProfileImageDatabase by lazy {
        ProfileImageDatabase.getInstance(this)
    }
}