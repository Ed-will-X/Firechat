package com.varsel.firechat

import android.app.Application
import com.varsel.firechat.model.ProfileImage.ProfileImageDatabase
import com.varsel.firechat.model.Setting.SettingDatabase

class FirechatApplication: Application() {
    val settingsDatabase: SettingDatabase by lazy {
        SettingDatabase.getInstance(this)
    }

    val profileImageDatabase: ProfileImageDatabase by lazy {
        ProfileImageDatabase.getInstance(this)
    }
}