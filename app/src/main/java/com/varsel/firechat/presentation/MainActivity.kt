package com.varsel.firechat.presentation

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActivityMainBinding
import com.varsel.firechat.domain.use_case._util.theme.SetThemeConfiguration_UseCase
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import com.varsel.firechat.presentation.signedOut.SignedoutActivity
import com.varsel.firechat.presentation.signedOut.fragments.AuthType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    lateinit var datastore: DataStore<Preferences>

    @Inject
    lateinit var setThemeConfiguration: SetThemeConfiguration_UseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        mAuth = FirebaseAuth.getInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)

        datastore = createDataStore(getString(R.string.settings).toLowerCase())

        // TODO Enable Later
//        setThemeConfiguration(datastore, lifecycleScope)

        // TODO: Remove
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        hideStatusBar()

        determineAuth()

//        setContentView(binding.root)
    }

    private fun loadGif(){
        if(isDarkTheme()){
//            Glide.with(this).load(R.drawable.speech_bubble_inverted).into(binding.splashIcon)
        } else {
            Glide.with(this).load(R.drawable.speech_bubble).into(binding.splashIcon)
        }
    }

    private fun isDarkTheme(): Boolean{
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                return true
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                return false
            }
        }

        return false
    }

    private fun determineAuth(){
        if (mAuth.currentUser?.uid != null){
            navigateToSignin()
        } else {
            navigateToSignup()
        }
    }

    private fun hideStatusBar(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun navigateToSignin(){
        val intent = Intent(this, SignedinActivity::class.java)

        intent.putExtra("AUTH_TYPE", AuthType.NAVIGATE_TO_SIGNED_IN)
        startActivity(intent)
        finish()
    }

    private fun navigateToSignup(){
        val intent = Intent(this, SignedoutActivity::class.java)

        intent.putExtra("AUTH_TYPE", AuthType.NAVIGATE_TO_SIGN_UP)

        startActivity(intent)
        finish()
    }


}