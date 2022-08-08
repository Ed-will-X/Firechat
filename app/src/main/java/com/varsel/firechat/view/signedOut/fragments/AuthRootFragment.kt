package com.varsel.firechat.view.signedOut.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentAuthRootBinding

class AuthRootFragment : Fragment() {
    private var _binding: FragmentAuthRootBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthRootBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.navigateToSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_authRootFragment_to_authScreenFragment)
        }
        binding.navigateToSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_authRootFragment_to_authScreenFragment)
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}