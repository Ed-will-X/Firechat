package com.varsel.firechat.presentation.signedIn.fragments.screens.about_user

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.varsel.firechat.R
import com.varsel.firechat.databinding.FragmentAboutUserBinding
import com.varsel.firechat.data.local.User.UserEntity

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

    fun setBindings(binding: FragmentAboutUserBinding, user: UserEntity, context: Context){
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
}