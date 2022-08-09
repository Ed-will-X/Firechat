package com.varsel.firechat.view.signedIn.fragments.bottomNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentProfileBinding
import com.varsel.firechat.viewModel.AppbarTag
import com.varsel.firechat.viewModel.AppbarViewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val appbarViewModel: AppbarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        appbarViewModel.setPage(AppbarTag.PROFILE)
        appbarViewModel.setProfile(activity, context)

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}