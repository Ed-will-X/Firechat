package com.varsel.firechat.view.signedIn.fragments.viewPager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentFriendsBinding
import com.varsel.firechat.databinding.FragmentProfileBinding
import com.varsel.firechat.viewModel.AppbarTag
import com.varsel.firechat.viewModel.AppbarViewModel

class FriendsFragment : Fragment() {
    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private val appbarViewModel: AppbarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        val view = binding.root



        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}