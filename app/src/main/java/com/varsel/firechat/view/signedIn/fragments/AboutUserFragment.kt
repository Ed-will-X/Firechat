package com.varsel.firechat.view.signedIn.fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.transition.MaterialSharedAxis
import com.varsel.firechat.databinding.FragmentAboutUserBinding
import com.varsel.firechat.utils.AnimationUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.viewModel.AboutUserViewModel

class AboutUserFragment : Fragment() {
    private var _binding: FragmentAboutUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private val viewModel: AboutUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutUserBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity
        val userId = AboutUserFragmentArgs.fromBundle(requireArguments()).userId

        binding.userDetailsClickable.setOnClickListener {
            viewModel.setRecyclerViewVisible(binding)
        }

        parent.firebaseViewModel.selectedChatRoom.observe(viewLifecycleOwner, Observer {

        })

        binding.navToUserPage.setOnClickListener {
            viewModel.navigateToUserPage(view, userId)
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}