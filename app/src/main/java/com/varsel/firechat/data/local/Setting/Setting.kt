package com.varsel.firechat.data.local.Setting

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "setting_table")
class Setting {
    @PrimaryKey
    lateinit var userId: String

    // Data consumption
    var auto_download_image_message = false
    var auto_download_video_message = false
    var auto_download_gif_message = false
    var auto_download_audio_message = false
    var public_post_auto_download_limit = 4

    // Theme
    private var darkMode = false
    private var overrideSystemTheme = false

    // Language
    var language = Language.ENGLISH

    // Notifications
    var show_chat_notifications = true
    var show_group_notifications = true
    var show_friend_request_notifications = true
    var show_new_friend_notifications = true
    var show_group_add_notifications = true


    constructor(userId: String){
        this.userId = userId
    }

    // For reverting to defaults
    constructor(setting: Setting){
        this.userId = setting.userId
    }

    fun setDarkMode(value: Boolean){
        if(!overrideSystemTheme){
            darkMode = value
        }
    }

    fun getOSTheme(): Boolean{
        // TODO: Implement get system theme
        return true
    }

    fun setOverrideSystemTheme(value: Boolean){
        overrideSystemTheme = value
        if(value){
            darkMode = getOSTheme()
        }
    }

    fun getDarkMode(): Boolean{
        return darkMode
    }

    fun getOverrideSystemTheme(): Boolean {
        return overrideSystemTheme
    }
}

class Language(){
    companion object {
        val ENGLISH = 0
        val FRENCH = 1
        val SPANISH = 2
        val GERMAN = 4
        val KOREAN = 5
    }
}