package com.varsel.firechat.domain.use_case.auth

import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData

class ValidateSignUp_UseCase {
    val passwordText = MutableLiveData<String>("")
    val confirmPasswordText = MutableLiveData<String>("")
    val hasBeenClicked_signup = MutableLiveData<Boolean>(false)

    operator fun invoke(fullname: EditText?, email: EditText?, password: EditText?, confirmPassword: EditText?, btn: Button?, agreement: CheckBox?){
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
            passwordText.value = password.text.toString()
            isValidPassword = password?.text.toString().length >= 8
            btn?.isEnabled = enableSignupBtn(isValidFullname, isValidEmail, isValidPassword, isValidConfirmation, isValidAgreement)
        }

        confirmPassword?.doAfterTextChanged {
            confirmPasswordText.value = confirmPassword.text.toString()
            isValidConfirmation = confirmPassword?.text.toString().length >= 8
            btn?.isEnabled = enableSignupBtn(isValidFullname, isValidEmail, isValidPassword, isValidConfirmation, isValidAgreement)
        }

        agreement?.setOnCheckedChangeListener { buttonView, isChecked ->
            isValidAgreement = isChecked
            btn?.isEnabled = enableSignupBtn(isValidFullname, isValidEmail, isValidPassword, isValidConfirmation, isValidAgreement)
        }
    }

    private fun enableSignupBtn(fullname: Boolean, email: Boolean, password: Boolean, confirmPassword: Boolean, agreement: Boolean): Boolean{
        if(hasBeenClicked_signup.value == false){
            if(passwordText.value != confirmPasswordText.value){
                return false
            } else {
                return fullname && email && password && confirmPassword && agreement
            }
        } else {
            return false
        }
    }
}