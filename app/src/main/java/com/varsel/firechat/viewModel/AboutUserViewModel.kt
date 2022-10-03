package com.varsel.firechat.viewModel

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentAboutUserBinding
import com.varsel.firechat.model.User.User
import com.varsel.firechat.view.signedIn.fragments.AboutUserFragmentDirections

class AboutUserViewModel: ViewModel() {
    private var userDetailsVisible = MutableLiveData<Boolean>(false)

    fun setRecyclerViewVisible(binding: FragmentAboutUserBinding){
        userDetailsVisible.value = !userDetailsVisible.value!!
        toggleUserDetailsVisibility(binding)
    }

    private fun toggleUserDetailsVisibility(binding: FragmentAboutUserBinding){
        if(userDetailsVisible.value == true){
            binding.userDetailsHideable.visibility = View.VISIBLE
            rotateIcon(binding.userDetailsIconAnimatable)
        } else {
            binding.userDetailsHideable.visibility = View.GONE
            rotateBack(binding.userDetailsIconAnimatable)
        }
    }

    fun setBindings(binding: FragmentAboutUserBinding, user: User, context: Context){
        binding.userName.text = user.name
        binding.occupation.text = user.occupation ?: context.getString(R.string.no_occupation)
    }

    private fun setGender(binding: FragmentAboutUserBinding){

    }

    private fun rotateIcon(view: View){
        val animator = ObjectAnimator.ofFloat(view, View.ROTATION, 90f)
        animator.duration = 300
        animator.start()
    }

    private fun rotateBack(view: View){
        val animator = ObjectAnimator.ofFloat(view, View.ROTATION, 0f)
        animator.duration = 300
        animator.start()
    }

    fun navigateToUserPage(view: View, userId: String){
        val action = AboutUserFragmentDirections.actionAboutUserFragmentToOtherProfileFragment(userId)
        view.findNavController().navigate(action)
    }
}