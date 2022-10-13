package com.varsel.firechat

import android.app.Application
import android.util.Log
import com.varsel.firechat.model.Setting.SettingDatabase

class FirechatApplication: Application() {
    val database: SettingDatabase by lazy {
        SettingDatabase.getInstance(this)
    }
}