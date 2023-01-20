package com.varsel.firechat.domain.use_case._util.user

import androidx.fragment.app.Fragment
import com.varsel.firechat.R

class GetFirstName_UseCase {

    operator fun invoke(name: String, fragment: Fragment): String{
        val arr = name.split(" ").toTypedArray()
        return fragment.getString(R.string.about_user, arr[0])
    }
}