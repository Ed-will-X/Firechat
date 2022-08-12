package com.varsel.firechat.view.signedOut.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentAuthRootBinding
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedOut.SignedoutActivity
import com.varsel.firechat.viewModel.FirebaseViewModel
import com.varsel.firechat.viewModel.SignedoutViewModel
import kotlinx.coroutines.launch

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

        binding.navigateToSignIn.setOnClickListener {
            showSigninDialog {
                Log.d("M.FIRE", "${email_login}")
                firebaseViewModel.signin(email_login, password_login, parent.mAuth, {
                    navigate {

                    }
                }, {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show()
                })
            }
        }
        binding.navigateToSignUp.setOnClickListener {
            Log.d("M.FIRE"," ${signedOutViewModel.emailText.value}")

            showSignUpDialog() {
                firebaseViewModel.signUp(emailText, passwordText, parent.mAuth, {
                    firebaseViewModel.saveUser(emailText, passwordText, parent.mAuth.currentUser?.uid.toString() ,parent.mDbRef, {
                        navigate {

                        }
                    }, {
                        Toast.makeText(requireContext(), "Something went wrong with DB", Toast.LENGTH_LONG).show()
                    })
                }, {
                    Log.d("M.FIRE", "${emailText}")
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show()
                })
            }
        }

        return view
    }

    // TODO: Clear database on login:
    fun clearDatabase(){

    }

    fun showSigninDialog(callback: () -> Unit){
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.action_sheet_signin)
        val email = dialog.findViewById<EditText>(R.id.email_edit_text)
        val password = dialog.findViewById<EditText>(R.id.password_edit_text)
        val btn = dialog.findViewById<Button>(R.id.sign_in_btn_actionsheet)

        signedOutViewModel.validateSignin(email, password, btn)

        email?.doAfterTextChanged {
            email_login = it.toString()
        }

        password?.doAfterTextChanged {
            password_login = it.toString()
        }

        btn?.setOnClickListener {
            callback()
        }

        dialog.show()
    }

    fun showSignUpDialog(callback: ()-> Unit){
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.action_sheet_signup)
        val agreement = dialog.findViewById<CheckBox>(R.id.agreement)
        val btn = dialog.findViewById<Button>(R.id.sign_up_btn_actionsheet)!!
        val fullname = dialog.findViewById<EditText>(R.id.name_edit_text)
        val email = dialog.findViewById<EditText>(R.id.email_edit_text)
        val password = dialog.findViewById<EditText>(R.id.password_edit_text)
        val confirmPassword = dialog.findViewById<EditText>(R.id.confirm_password_edit_text)

        fullname?.doAfterTextChanged {
            fullnameText = it.toString()
        }

        email?.doAfterTextChanged {
            emailText = it.toString()
        }

        password?.doAfterTextChanged {
            passwordText = it.toString()
        }

        signedOutViewModel.validateSignup(fullname, email, password, confirmPassword, btn, agreement)

        btn.setOnClickListener {
            callback()
        }

        dialog.show()
    }

    fun navigate(callback: () -> Unit){
        val intent = Intent(context, SignedinActivity::class.java)
        callback()

        parent.finish()
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}