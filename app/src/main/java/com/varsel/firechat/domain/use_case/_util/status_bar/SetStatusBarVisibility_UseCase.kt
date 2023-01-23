package com.varsel.firechat.domain.use_case._util.status_bar

import android.view.WindowManager
import com.varsel.firechat.presentation.signedIn.SignedinActivity

sealed class StatusBarVisibility {
    class Show(): StatusBarVisibility()
    class Hide(): StatusBarVisibility()
}

class SetStatusBarVisibility_UseCase {
    operator fun invoke(visibility: StatusBarVisibility, activity: SignedinActivity) {
        when(visibility) {
            is StatusBarVisibility.Show -> {
                showStatusBar(activity)
            }
            is StatusBarVisibility.Hide -> {
                hideStatusBar(activity)
            }
        }
    }

    fun hideStatusBar(activity: SignedinActivity){
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun showStatusBar(activity: SignedinActivity){
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}