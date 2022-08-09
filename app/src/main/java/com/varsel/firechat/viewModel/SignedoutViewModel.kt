package com.varsel.firechat.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedOut.SignedoutActivity

class SignedoutViewModel: ViewModel() {
    val isValidSignIn = MutableLiveData<Boolean>(false)

    fun showSigninDialog(context: Context, fragment: Fragment?, activity: Activity?){
        val dialog = BottomSheetDialog(context)
        dialog.setContentView(R.layout.action_sheet_signin)
        val email = dialog.findViewById<EditText>(R.id.email_edit_text)
        val password = dialog.findViewById<EditText>(R.id.password_edit_text)
        val btn = dialog.findViewById<Button>(R.id.sign_in_btn_actionsheet)

        validateSignin(email, password, btn)

        btn?.setOnClickListener {
            val intent = Intent(context, SignedinActivity::class.java)
            fragment?.startActivity(intent)
            activity?.finish()
        }

        dialog.show()
    }

    fun showSignUpDialog(context: Context, fragment: Fragment?, activity: Activity?){
        val dialog = BottomSheetDialog(context)
        dialog.setContentView(R.layout.action_sheet_signup)
        val agreement = dialog.findViewById<CheckBox>(R.id.agreement)
        val btn = dialog.findViewById<Button>(R.id.sign_up_btn_actionsheet)!!
        val fullname = dialog.findViewById<EditText>(R.id.name_edit_text)
        val email = dialog.findViewById<EditText>(R.id.email_edit_text)
        val password = dialog.findViewById<EditText>(R.id.password_edit_text)
        val confirmPassword = dialog.findViewById<EditText>(R.id.confirm_password_edit_text)

        validateSignup(fullname, email, password, confirmPassword, btn, agreement)

        btn.setOnClickListener {
            val intent = Intent(context, SignedinActivity::class.java)
            activity?.finish()
            fragment?.startActivity(intent)
        }

        dialog.show()
    }

    private fun validateSignup(fullname: EditText?, email: EditText?, password: EditText?, confirmPassword: EditText?, btn: Button?, agreement: CheckBox?){
        var isValidFullname = false
        var isValidEmail = false
        var isValidPassword = false
        var isValidConfirmation = false
        var isValidAgreement = false

        fullname?.doAfterTextChanged {
            isValidFullname = fullname?.text.toString().length >= 5
            btn?.isEnabled = enableSignupBtn(isValidFullname, isValidEmail, isValidPassword, isValidConfirmation, isValidAgreement)
        }

        email?.doAfterTextChanged {
            isValidEmail = email?.text.toString().length >= 3
            btn?.isEnabled = enableSignupBtn(isValidFullname, isValidEmail, isValidPassword, isValidConfirmation, isValidAgreement)
        }

        password?.doAfterTextChanged {
            isValidPassword = password?.text.toString().length >= 8
            btn?.isEnabled = enableSignupBtn(isValidFullname, isValidEmail, isValidPassword, isValidConfirmation, isValidAgreement)
        }

        confirmPassword?.doAfterTextChanged {
            isValidConfirmation = confirmPassword?.text.toString().length >= 8
            btn?.isEnabled = enableSignupBtn(isValidFullname, isValidEmail, isValidPassword, isValidConfirmation, isValidAgreement)
        }

        agreement?.setOnCheckedChangeListener { buttonView, isChecked ->
            isValidAgreement = isChecked
            btn?.isEnabled = enableSignupBtn(isValidFullname, isValidEmail, isValidPassword, isValidConfirmation, isValidAgreement)
        }
    }

    private fun validateSignin(email: EditText?, password: EditText?, btn: Button?){
        var isValidEmail = false
        var isValidPassword = false

        email?.doAfterTextChanged {
            isValidEmail = email?.text.toString().length >= 3
            btn?.isEnabled = enableSigninBtn(isValidEmail, isValidPassword)
        }

        password?.doAfterTextChanged {
            isValidPassword = password?.text.toString().length >= 8
            btn?.isEnabled = enableSigninBtn(isValidEmail, isValidPassword)
        }
    }

    private fun enableSignupBtn(fullname: Boolean, email: Boolean, password: Boolean, confirmPassword: Boolean, agreement: Boolean): Boolean{
        return fullname && email && password && confirmPassword && agreement
    }

    private fun enableSigninBtn(email: Boolean, password: Boolean): Boolean{
        return email && password
    }
}