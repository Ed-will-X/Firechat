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