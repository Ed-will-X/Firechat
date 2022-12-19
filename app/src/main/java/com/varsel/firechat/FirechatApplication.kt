package com.varsel.firechat

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.varsel.firechat.data.local.Image.ImageDatabase
import com.varsel.firechat.data.local.ProfileImage.ProfileImageDatabase
import com.varsel.firechat.data.local.PublicPost.PublicPostDatabase
import com.varsel.firechat.data.local.ReadReceipt.ReadReceiptDatabase
import com.varsel.firechat.data.local.Setting.SettingDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FirechatApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        enablePersistence()
    }

    fun enablePersistence(){
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}