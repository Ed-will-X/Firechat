package com.varsel.firechat

import android.app.Application
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.varsel.firechat.model.Image.ImageDatabase
import com.varsel.firechat.model.ProfileImage.ProfileImageDatabase
import com.varsel.firechat.model.PublicPost.PublicPostDatabase
import com.varsel.firechat.model.Setting.SettingDatabase

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

    override fun onCreate() {
        super.onCreate()
        enablePersistence()
    }

    fun enablePersistence(){
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}