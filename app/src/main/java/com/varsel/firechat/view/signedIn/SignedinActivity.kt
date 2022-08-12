package com.varsel.firechat.view.signedIn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.createDataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActivitySignedinBinding
import com.varsel.firechat.viewModel.AppbarViewModel
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


class SignedinActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignedinBinding
    private lateinit var appbarViewModel: AppbarViewModel
    lateinit var dataStore: DataStore<Preferences>
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance()

        // initialising the datastore
        dataStore = createDataStore("current_user")

        binding = ActivitySignedinBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        appbarViewModel = ViewModelProvider(this).get(AppbarViewModel::class.java)

        binding.appbarViewModel = appbarViewModel
        binding.lifecycleOwner = this

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.signed_in_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavView.setupWithNavController(navController)

        // Makes the chat fragment the default destination
        binding.bottomNavView.menu.findItem(R.id.chatsFragment).setChecked(true)
    }
}