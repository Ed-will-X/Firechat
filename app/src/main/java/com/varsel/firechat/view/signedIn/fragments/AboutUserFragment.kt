package com.varsel.firechat.view.signedIn.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentAboutUserBinding
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.viewModel.AboutUserViewModel

class AboutUserFragment : Fragment() {
    private var _binding: FragmentAboutUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private val viewModel: AboutUserViewModel by activityViewModels()
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutUserBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity
        userId = AboutUserFragmentArgs.fromBundle(requireArguments()).userId

        binding.userDetailsClickable.setOnClickListener {
            viewModel.setRecyclerViewVisible(binding)
        }

        parent.firebaseViewModel.selectedChatRoom.observe(viewLifecycleOwner, Observer {

        })

        observeUserProps()
        setBindings()

        return view
    }

    private fun setBindings(){
        binding.navToUserPage.setOnClickListener {
            viewModel.navigateToUserPage(binding.root, userId)
        }

        binding.backButton.setOnClickListener {
            popNavigation()
        }
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    private fun observeUserProps(){
        parent.profileImageViewModel.selectedOtherUserProfilePicChat.observe(viewLifecycleOwner, Observer {
            if(it != null){
                ImageUtils.setProfilePic(it, binding.profileImage, binding.profileImageParent)
            }
        })

        parent.firebaseViewModel.selectedChatRoomUser.observe(viewLifecycleOwner, Observer {
            binding.userName.text = it?.name
            binding.occupation.text = it?.occupation ?: context?.getString(R.string.no_occupation)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}