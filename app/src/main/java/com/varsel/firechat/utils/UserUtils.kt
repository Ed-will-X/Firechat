package com.varsel.firechat.utils

import android.content.Context
import android.provider.Settings.Global.getString
import androidx.fragment.app.Fragment
import com.varsel.firechat.R
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class UserUtils(var fragment: Fragment) {
    companion object {
        fun truncate(about: String, length: Int): String{
            if(about.length > length){
                return "${about.subSequence(0, length)}..."
            } else {
                return about
            }
        }
    }

    fun getFirstName(name: String): String{
        val arr = name.split(" ").toTypedArray()
        return fragment.getString(R.string.about_user, arr[0])
    }
}