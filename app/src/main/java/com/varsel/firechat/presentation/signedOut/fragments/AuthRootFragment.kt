package com.varsel.firechat.presentation.signedOut.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.common.Response
import com.varsel.firechat.databinding.ActionSheetSigninBinding
import com.varsel.firechat.databinding.ActionSheetSignupBinding
import com.varsel.firechat.databinding.FragmentAuthRootBinding
import com.varsel.firechat.domain.use_case.auth.SignUp_UseCase
import com.varsel.firechat.domain.use_case.auth.ValidateSignIn_UseCase
import com.varsel.firechat.domain.use_case.auth.ValidateSignUp_UseCase
import com.varsel.firechat.domain.use_case.current_user.SignIn_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedOut.SignedoutActivity
import com.varsel.firechat.presentation.signedOut.SignedoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class AuthType {
    companion object {
        val SIGN_UP = "SIGN_UP"
        val SIGN_IN = "SIGN_IN"
        val NAVIGATE_TO_SIGN_UP = "NAVIGATE_TO_SIGN_UP"
        val NAVIGATE_TO_SIGNED_IN = "NAVIGATE_TO_SIGNED_IN"
    }
}

@AndroidEntryPoint
class AuthRootFragment : Fragment() {
    private var _binding: FragmentAuthRootBinding? = null
    private lateinit var parent: SignedoutActivity
    private val binding get() = _binding!!
    private val signedOutViewModel: SignedoutViewModel by activityViewModels()
    private var fullnameText = ""
    private var emailText = ""
    private var passwordText = ""

    private var email_login = ""
    private var password_login = ""

    @Inject
    lateinit var signin: SignIn_UseCase

    @Inject
    lateinit var signup: SignUp_UseCase

    @Inject
    lateinit var validateSignup: ValidateSignUp_UseCase

    @Inject
    lateinit var validateSignin: ValidateSignIn_UseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthRootBinding.inflate(inflater, container, false)
        val view = binding.root

        parent  = activity as SignedoutActivity

        Glide.with(requireContext())
            .load(R.drawable.a_s_s_d_f_r_v)
            .apply(RequestOptions().override(720, 720))
            .into(binding.backgroundImage)

        binding.signIn.setOnClickListener {
            showSigninDialog { dialogBinding ->
                dialogBinding.signInBtnActionsheet.isEnabled = false

                signin(email_login, password_login).onEach {
                    when(it) {
                        is Response.Success -> {
                            navigate(AuthType.SIGN_IN) {

                            }
                            dialogBinding.signInBtnActionsheet.isEnabled = true
                        }
                        is Response.Fail -> {
                            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show()
                            dialogBinding.signInBtnActionsheet.isEnabled = true
                        }
                    }
                }.launchIn(lifecycleScope)
            }
        }
        binding.signUp.setOnClickListener {
            showSignUpDialog { dialogBinding ->
                dialogBinding.signUpBtnActionsheet.isEnabled = false

                signup(fullnameText, emailText, passwordText).onEach {
                    when(it) {
                        is Response.Success -> {
                            navigate(AuthType.SIGN_UP) {

                            }
                            dialogBinding.signUpBtnActionsheet.isEnabled = true
                        }
                        is Response.Loading -> {

                        }
                        is Response.Fail -> {
                            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show()
                            dialogBinding.signUpBtnActionsheet.isEnabled = true
                        }
                    }
                }.launchIn(lifecycleScope)
            }
        }

        return view
    }

    fun showSigninDialog(callback: (dialogBinding: ActionSheetSigninBinding) -> Unit){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = ActionSheetSigninBinding.inflate(layoutInflater, binding.root, false)
        dialog.setContentView(dialogBinding.root)

        validateSignin(dialogBinding.emailEditText, dialogBinding.passwordEditText, dialogBinding.signInBtnActionsheet)

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

        validateSignup(dialogBinding.nameEditText, dialogBinding.emailEditText, dialogBinding.passwordEditText, dialogBinding.confirmPasswordEditText, dialogBinding.signUpBtnActionsheet, dialogBinding.agreement)

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