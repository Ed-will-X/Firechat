package com.varsel.firechat.view.signedIn.fragments.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentStorageAndHistoryBinding

class StorageAndHistory : Fragment() {
    private var _binding: FragmentStorageAndHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStorageAndHistoryBinding.inflate(layoutInflater, container, false)
        val view = binding.root


        binding.backButton.setOnClickListener {
            popNavigation()
        }

        return view
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}