package com.varsel.firechat.view.signedOut.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.varsel.firechat.databinding.FragmentAuthScreenBinding
import com.varsel.firechat.view.signedIn.SignedinActivity

class AuthScreenFragment : Fragment() {
    private var _binding: FragmentAuthScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthScreenBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.signInButton.setOnClickListener {
            val intent = Intent(activity, SignedinActivity::class.java)
            activity?.finish()
            startActivity(intent)
        }

        binding.signUpButton.setOnClickListener {
            val intent = Intent(activity, SignedinActivity::class.java)
            activity?.finish()
            startActivity(intent)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}