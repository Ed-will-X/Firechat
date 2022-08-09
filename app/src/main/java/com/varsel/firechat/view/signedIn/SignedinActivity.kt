package com.varsel.firechat.view.signedIn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActivitySignedinBinding
import com.varsel.firechat.viewModel.AppbarViewModel

class SignedinActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignedinBinding
    private lateinit var appbarViewModel: AppbarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignedinBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        appbarViewModel = ViewModelProvider(this).get(AppbarViewModel::class.java)

        binding.appbarViewModel = appbarViewModel
        binding.lifecycleOwner = this

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.signed_in_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // passes the appbar heights to the viewModel
        appbarViewModel.getAppbarDefault(resources.getDimensionPixelSize(R.dimen.app_bar_height))
        appbarViewModel.getAppbarExtended(resources.getDimensionPixelSize(R.dimen.app_bar_height_extended))

        binding.bottomNavView.setupWithNavController(navController)

        // Makes the chat fragment the default destination
        binding.bottomNavView.menu.findItem(R.id.chatsFragment).setChecked(true)
    }
}