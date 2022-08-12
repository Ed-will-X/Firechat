package com.varsel.firechat.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedOut.SignedoutActivity
import kotlinx.coroutines.launch

class SignedoutViewModel: ViewModel() {
    val emailText = MutableLiveData<String>("")
    val passwordText = MutableLiveData<String>("")
    val nameText = MutableLiveData<String>("")
    val confirmPasswordText = MutableLiveData<String>("")

    fun validateSignup(fullname: EditText?, email: EditText?, password: EditText?, confirmPassword: EditText?, btn: Button?, agreement: CheckBox?){
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

    fun validateSignin(email: EditText?, password: EditText?, btn: Button?){
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