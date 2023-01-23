package com.varsel.firechat.domain.use_case._util.status_bar

import android.os.Build
import android.view.WindowManager
import androidx.core.view.ViewCompat
import com.varsel.firechat.presentation.signedIn.SignedinActivity

class ChangeStatusBarColor_UseCase {
    operator fun invoke(color: Int, light: Boolean, activity: SignedinActivity){
        if (Build.VERSION.SDK_INT >= 21) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.setStatusBarColor(activity.getResources().getColor(color))

            // new api
            ViewCompat.getWindowInsetsController(activity.window.decorView)?.apply {
                // Light text == dark status bar
                isAppearanceLightStatusBars = light
            }
            // old api (Uncomment the cole below if the status bar is buggy on older devices)
//            val decorView = window.decorView
//            decorView.systemUiVisibility =
//                if (light) {
//                    decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
//                } else {
//                    decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                }
        }
    }
}