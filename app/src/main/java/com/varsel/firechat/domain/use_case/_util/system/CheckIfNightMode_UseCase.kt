package com.varsel.firechat.domain.use_case._util.system

import android.content.res.Configuration
import com.varsel.firechat.presentation.signedIn.SignedinActivity

class CheckIfNightMode_UseCase {
    operator fun invoke(activity: SignedinActivity): Boolean {
        when (activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                return true
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                return false
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                return false
            }
            else -> {
                return false
            }
        }
    }
}