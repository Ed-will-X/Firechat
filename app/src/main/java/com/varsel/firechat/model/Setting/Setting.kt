package com.varsel.firechat.model.Setting

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "setting_table")
class Setting {
    @PrimaryKey(autoGenerate = false)
    lateinit var settingName: String

    var settingValue: String?

    constructor(settingName: String, settingValue: String?){
        this.settingName = settingName
        this.settingValue = settingValue
    }
}

class SettingKey(){
    companion object {
        val PROFILE_PICTURE = "PROFILE_PICTURE"
        val DARK_MODE = "DARK_MODE"
    }
}