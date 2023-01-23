package com.varsel.firechat.domain.use_case.auth

import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData

class ValidateSignIn_UseCase {
    val hasBeenClicked_signin = MutableLiveData<Boolean>(false)

    operator fun invoke(email: EditText?, password: EditText?, btn: Button?){
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

    private fun enableSigninBtn(email: Boolean, password: Boolean): Boolean{
        if(hasBeenClicked_signin.value == false){
            return email && password
        } else {
            return false
        }
    }
}