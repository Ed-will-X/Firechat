package com.varsel.firechat.domain.use_case._util

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.varsel.firechat.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InfobarColors {
    companion object {
        const val OFFLINE = R.color.orange_red
        const val ONLINE = R.color.light_green_2
        const val UPLOADING = R.color.deep_blue
        const val SUCCESS = R.color.light_green_2
        const val FAILURE = R.color.orange_red
        const val NEW_MESSAGE = R.color.purple_700
        const val NEW_FRIEND = R.color.purple_700
        const val NEW_FRIEND_REQUEST = R.color.dark_lemon
        const val GROUP_ADD = R.color.dark_lemon
    }
}

class InfobarControllerUseCase(
    val lifecycleOwner: LifecycleOwner,
    val activity: Activity,
    val bottomInfobar: View,
    val infobarText: TextView
) {
    fun showBottomInfobar(
        customString: String?,
        customColor: Int?,
    ){
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            setInfobarProps(customString, customColor)
            bottomInfobar.visibility = View.VISIBLE

            delay(3000)

            bottomInfobar.visibility = View.GONE
        }
    }

    private fun setInfobarProps(customString: String? = null, customColor: Int? = null){
        this.infobarText.text = customString
        this.bottomInfobar.setBackgroundColor(activity.resources.getColor(customColor ?: R.color.black))
    }

}