package com.varsel.firechat.view.signedOut.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActionSheetSigninBinding
import com.varsel.firechat.databinding.ActionSheetSignupBinding
import com.varsel.firechat.databinding.FragmentAuthRootBinding
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedOut.SignedoutActivity
import com.varsel.firechat.viewModel.FirebaseViewModel
import com.varsel.firechat.viewModel.SignedoutViewModel

class AuthType {
    companion object {
        val SIGN_UP = "SIGN_UP"
        val SIGN_IN = "SIGN_IN"
        val NAVIGATE_TO_SIGN_UP = "NAVIGATE_TO_SIGN_UP"
        val NAVIGATE_TO_SIGNED_IN = "NAVIGATE_TO_SIGNED_IN"
    }
}

class AuthRootFragment : Fragment() {
    private var _binding: FragmentAuthRootBinding? = null
    private lateinit var parent: SignedoutActivity
    private val binding get() = _binding!!
    private val signedOutViewModel: SignedoutViewModel by activityViewModels()
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private var fullnameText = ""
    private var emailText = ""
    private var passwordText = ""

    private var email_login = ""
    private var password_login = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthRootBinding.inflate(inflater, container, false)
        val view = binding.root

        parent  = activity as SignedoutActivity

        binding.signIn.setOnClickListener {
            showSigninDialog { dialogBinding ->
                signedOutViewModel.hasBeenClicked_signin.value = true
                dialogBinding.signInBtnActionsheet.isEnabled = false

                firebaseViewModel.signin(email_login, password_login, parent.mAuth, {
                    navigate(AuthType.SIGN_IN) {

                    }
                    dialogBinding.signInBtnActionsheet.isEnabled = true
                    signedOutViewModel.hasBeenClicked_signin.value = false
                }, {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show()
                    dialogBinding.signInBtnActionsheet.isEnabled = true
                    signedOutViewModel.hasBeenClicked_signin.value = false
                })
            }
        }
        binding.signUp.setOnClickListener {

            showSignUpDialog() { dialogBinding ->
                dialogBinding.signUpBtnActionsheet.isEnabled = false
                signedOutViewModel.hasBeenClicked_signup.value = true

                firebaseViewModel.signUp(emailText, passwordText, parent.mAuth, {
                    firebaseViewModel.saveUser(fullnameText, emailText, parent.mAuth.currentUser?.uid.toString() ,parent.mDbRef, {
                        navigate(AuthType.SIGN_UP) {

                        }
                        dialogBinding.signUpBtnActionsheet.isEnabled = true
                        signedOutViewModel.hasBeenClicked_signup.value = false
                    }, {
                        Toast.makeText(requireContext(), "Something went wrong with DB", Toast.LENGTH_LONG).show()
                        dialogBinding.signUpBtnActionsheet.isEnabled = true
                        signedOutViewModel.hasBeenClicked_signup.value = false
                    })
                }, {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show()
                    dialogBinding.signUpBtnActionsheet.isEnabled = true
                    signedOutViewModel.hasBeenClicked_signup.value = false
                })
            }
        }

        return view
    }

    // TODO: Clear database on login:
    fun clearDatabase(){

    }

    fun showSigninDialog(callback: (dialogBinding: ActionSheetSigninBinding) -> Unit){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetSigninBinding.inflate(layoutInflater, binding.root, false)
        dialog.setContentView(dialogBinding.root)

        signedOutViewModel.validateSignin(dialogBinding.emailEditText, dialogBinding.passwordEditText, dialogBinding.signInBtnActionsheet)

        dialogBinding.emailEditText.doAfterTextChanged {
            email_login = it.toString()
        }

        dialogBinding.passwordEditText.doAfterTextChanged {
            password_login = it.toString()
        }

        dialogBinding.signInBtnActionsheet.setOnClickListener {
            val sanitisedEmail = email_login.trim()

            email_login = sanitisedEmail
            callback(dialogBinding)
        }

        dialog.show()
    }

    fun showSignUpDialog(callback: (dialogBinding: ActionSheetSignupBinding)-> Unit){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetSignupBinding.inflate(layoutInflater, binding.root, false)
        dialog.setContentView(dialogBinding.root)


        dialogBinding.nameEditText.doAfterTextChanged {
            fullnameText = it.toString()
        }

        dialogBinding.emailEditText.doAfterTextChanged {
            emailText = it.toString()
        }

        dialogBinding.passwordEditText.doAfterTextChanged {
            passwordText = it.toString()
        }

        signedOutViewModel.validateSignup(dialogBinding.nameEditText, dialogBinding.emailEditText, dialogBinding.passwordEditText, dialogBinding.confirmPasswordEditText, dialogBinding.signUpBtnActionsheet, dialogBinding.agreement)

        dialogBinding.signUpBtnActionsheet.setOnClickListener {
            val sanitisedFullname = fullnameText.trim()
            val sanitisedEmail = emailText.trim()

            fullnameText = sanitisedFullname
            emailText = sanitisedEmail

            callback(dialogBinding)
        }

        dialog.show()
    }

    fun navigate(authType: String, callback: () -> Unit){
        val intent = Intent(context, SignedinActivity::class.java)
        callback()

        intent.putExtra("AUTH_TYPE", authType)

        parent.finish()
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}