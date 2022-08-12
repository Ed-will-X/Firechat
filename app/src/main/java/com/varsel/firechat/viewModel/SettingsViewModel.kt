package com.varsel.firechat.viewModel

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.varsel.firechat.R
import com.varsel.firechat.view.signedOut.SignedoutActivity
import kotlin.math.log

class SettingsViewModel: ViewModel() {


    fun showCustomLogoutConfirmationDialog(context: Context, fragment: Fragment){
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_logout)

        dialog.show()

    }

    fun logout(activity: FragmentActivity, context: Context?, callback: ()-> Unit){
        val intent = Intent(context, SignedoutActivity::class.java)
        callback()

        activity.startActivity(intent)
        activity.finish()

        // run firebase logout
    }
}