package com.varsel.firechat

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.varsel.firechat.data.local.Image.ImageDatabase
import com.varsel.firechat.data.local.ProfileImage.ProfileImageDatabase
import com.varsel.firechat.data.local.PublicPost.PublicPostDatabase
import com.varsel.firechat.data.local.ReadReceipt.ReadReceiptDatabase
import com.varsel.firechat.data.local.Setting.SettingDatabase

class FirechatApplication: Application() {
    val settingsDatabase: SettingDatabase by lazy {
        SettingDatabase.getInstance(this)
    }

    val profileImageDatabase: ProfileImageDatabase by lazy {
        ProfileImageDatabase.getInstance(this)
    }

    val imageDatabase: ImageDatabase by lazy {
        ImageDatabase.getInstance(this)
    }

    val publicPostDatabase: PublicPostDatabase by lazy {
        PublicPostDatabase.getInstance(this)
    }

    val readReceiptsDatabase: ReadReceiptDatabase by lazy {
        ReadReceiptDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        enablePersistence()
    }

    fun enablePersistence(){
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}