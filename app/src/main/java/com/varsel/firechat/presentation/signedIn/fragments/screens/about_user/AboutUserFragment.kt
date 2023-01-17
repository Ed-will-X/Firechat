package com.varsel.firechat.presentation.signedIn.fragments.screens.about_user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentAboutUserBinding
import com.varsel.firechat.data.local.User.User
import com.varsel.firechat.utils.ImageUtils
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.utils.ExtensionFunctions.Companion.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutUserFragment : Fragment() {
    private var _binding: FragmentAboutUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var parent: SignedinActivity
    private lateinit var viewModel: AboutUserViewModel
    private lateinit var userId: String
    private var userImg: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutUserBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        parent = activity as SignedinActivity
        userId = AboutUserFragmentArgs.fromBundle(requireArguments()).userId

        viewModel = ViewModelProvider(this).get(AboutUserViewModel::class.java)
        viewModel.getUser(userId)
        collectState()

        binding.userDetailsClickable.setOnClickListener {
            viewModel.setRecyclerViewVisible(binding)
        }

        setBindings()

        return view
    }

    private fun collectState() {
        collectLatestLifecycleFlow(viewModel.state) {
            if(it.selectedUser != null) {
                observeUserProps(it.selectedUser)
            }

        }
    }

    private fun setBindings(){
        binding.navToUserPage.setOnClickListener {
            navigateToUserPage(binding.root, userId)
        }

        binding.backButton.setOnClickListener {
            popNavigation()
        }
    }

    private fun navigateToUserPage(view: View, userId: String){
        val action = AboutUserFragmentDirections.actionAboutUserFragmentToOtherProfileFragment(userId)
        parent.profileImageViewModel.selectedOtherUserProfilePic.value = userImg

        view.findNavController().navigate(action)
    }

    private fun popNavigation(){
        findNavController().navigateUp()
    }

    private fun observeUserProps(user: User){
        parent.profileImageViewModel.selectedOtherUserProfilePicChat.observe(viewLifecycleOwner, Observer {
            if(it != null){
                ImageUtils.setProfilePic(it, binding.profileImage, binding.profileImageParent, parent)
                userImg = it
            }
        })

        binding.userName.text = user.name
        binding.occupation.text = user.occupation ?: context?.getString(R.string.no_occupation)
        bindUserDetailProps(user)
    }

    private fun bindUserDetailProps(user: User){
        val noneColor = resources.getColor(R.color.transparent_grey)
        if(user.name.isNotEmpty()){
            binding.nameChecked.visibility = View.VISIBLE
            binding.detailName.text = user.name
        } else {
            binding.nameChecked.visibility = View.GONE
            binding.detailName.text = getString(R.string.none)
            binding.detailName.setTextColor(noneColor)
        }

        if(user.email.isNotEmpty()){
            binding.emailChecked.visibility = View.VISIBLE
            binding.detailEmail.text = user.email
        } else {
            binding.emailChecked.visibility = View.GONE
            binding.detailEmail.text = getString(R.string.none)
            binding.detailEmail.setTextColor(noneColor)
        }

        if(user.phone?.isNotEmpty() == true){
            binding.phoneChecked.visibility =  View.VISIBLE
            binding.detailPhone.text = user.phone
        } else {
            binding.phoneChecked.visibility =  View.GONE
            binding.detailPhone.text = getString(R.string.none)
            binding.detailPhone.setTextColor(noneColor)
        }

        if(user.occupation?.isNotEmpty() == true){
            binding.occupationChecked.visibility = View.VISIBLE
            binding.detailOccupation.text = user.occupation
        } else {
            binding.occupation.visibility = View.GONE
            binding.detailOccupation.text = getString(R.string.none)
            binding.detailOccupation.setTextColor(noneColor)
        }

        if(user.location?.isNotEmpty() == true){
            binding.locationChecked.visibility = View.VISIBLE
            binding.detailLocation.text = user.location
        } else {
            binding.locationChecked.visibility = View.GONE
            binding.detailLocation.text = getString(R.string.none)
            binding.detailLocation.setTextColor(noneColor)
        }

        binding.genderChecked.visibility = View.GONE
        binding.detailGender.text = getString(R.string.none)
        binding.detailGender.setTextColor(noneColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}