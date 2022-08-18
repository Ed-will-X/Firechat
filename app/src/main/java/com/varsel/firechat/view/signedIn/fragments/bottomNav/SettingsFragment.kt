package com.varsel.firechat.view.signedIn.fragments.bottomNav

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentFriendsBinding
import com.varsel.firechat.databinding.FragmentSettingsBinding
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.viewModel.*
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private lateinit var parent: SignedinActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        parent = activity as SignedinActivity
        val view = binding.root

        binding.settingsLogoutClickable.setOnClickListener {
            Toast.makeText(view.context, "Logout", Toast.LENGTH_LONG).show()
            showLogoutConfirmationDialog {
                lifecycleScope.launch {
                    firebaseViewModel.signOut((parent as SignedinActivity).firebaseAuth)
                }
            }
        }

        return view
    }

    fun showLogoutConfirmationDialog(callback: ()-> Unit){
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(R.string.logout_dialog_title))
                .setMessage(getString(R.string.logout_dialog_message))
                .setCancelable(true)
                .setNegativeButton (getString(R.string.logout_dialog_no)){ _, _ ->

                }
                .setPositiveButton(getString(R.string.logout_dialog_yes)){ _, _ ->
                    activity?.let { it1 -> settingsViewModel.logout(it1, context, callback) }
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}