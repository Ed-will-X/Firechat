package com.varsel.firechat.view.signedOut.fragments

import android.content.Context
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
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentAuthRootBinding
import com.varsel.firechat.view.signedOut.SignedoutActivity
import com.varsel.firechat.viewModel.SignedoutViewModel

class AuthRootFragment : Fragment() {
    private var _binding: FragmentAuthRootBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignedoutViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthRootBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.navigateToSignIn.setOnClickListener {
            viewModel.showSigninDialog(requireContext(), parentFragment, activity)
        }
        binding.navigateToSignUp.setOnClickListener {
            viewModel.showSignUpDialog(requireContext(), parentFragment, activity)
        }

        return view
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}