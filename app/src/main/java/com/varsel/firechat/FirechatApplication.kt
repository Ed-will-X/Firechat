package com.varsel.firechat

import android.app.Application
import android.util.Log
import com.varsel.firechat.model.Image.ImageDatabase
import com.varsel.firechat.model.Setting.SettingDatabase

class FirechatApplication: Application() {
    val settingsDatabase: SettingDatabase by lazy {
        SettingDatabase.getInstance(this)
    }

    val imageDatabase: ImageDatabase by lazy {
        ImageDatabase.getInstance(this)
    }
}