package com.varsel.firechat.di

import android.app.Application
import androidx.room.Room
import com.varsel.firechat.data.local.Image.ImageDao
import com.varsel.firechat.data.local.Image.ImageDatabase
import com.varsel.firechat.data.local.ProfileImage.ProfileImageDao
import com.varsel.firechat.data.local.ProfileImage.ProfileImageDatabase
import com.varsel.firechat.data.local.PublicPost.PublicPostDao
import com.varsel.firechat.data.local.PublicPost.PublicPostDatabase
import com.varsel.firechat.data.local.ReadReceipt.ReadReceiptDao
import com.varsel.firechat.data.local.ReadReceipt.ReadReceiptDatabase
import com.varsel.firechat.data.local.Setting.SettingDao
import com.varsel.firechat.data.local.Setting.SettingDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideImageDatabase(app: Application): ImageDatabase {
        return Room.databaseBuilder(app.applicationContext, ImageDatabase:: class.java, "image_database").build()
    }

    @Provides
    @Singleton
    fun provideImageDao(imageDatabase: ImageDatabase): ImageDao {
        return imageDatabase.imageDao
    }

    @Provides
    @Singleton
    fun provideSettingDatabase(app: Application): SettingDatabase {
        return Room.databaseBuilder(app.applicationContext, SettingDatabase:: class.java, "settings_database").build()
    }

    @Provides
    @Singleton
    fun provideSettingDao(settingDatabase: SettingDatabase): SettingDao {
        return settingDatabase.settingDao
    }

    @Provides
    @Singleton
    fun provideProfileImageDatabase(app: Application): ProfileImageDatabase {
        return Room.databaseBuilder(app.applicationContext, ProfileImageDatabase:: class.java, "profile_image_database").build()
    }

    @Provides
    @Singleton
    fun provideProfileImageDao(profileImageDatabase: ProfileImageDatabase): ProfileImageDao {
        return profileImageDatabase.profileImageDao
    }

    @Provides
    @Singleton
    fun providePublicPostDatabase(app: Application): PublicPostDatabase {
        return Room.databaseBuilder(app.applicationContext, PublicPostDatabase:: class.java, "public_post_database").build()
    }

    @Singleton
    @Provides
    fun providePublicPostDao(publicPostDatabase: PublicPostDatabase): PublicPostDao {
        return publicPostDatabase.publicPostDao
    }

    @Provides
    @Singleton
    fun provideReadReceiptDatabase(app: Application): ReadReceiptDatabase {
        return Room.databaseBuilder(app.applicationContext, ReadReceiptDatabase:: class.java, "read_receipts_database").build()
    }

    @Provides
    @Singleton
    fun provideReadReceiptDao(readReceiptDatabase: ReadReceiptDatabase) : ReadReceiptDao {
        return readReceiptDatabase.readReceiptDao
    }
}