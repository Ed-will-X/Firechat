package com.varsel.firechat.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.varsel.firechat.R
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedOut.SignedoutActivity
import com.varsel.firechat.view.signedOut.fragments.AuthType
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance()

        determineAuth()

        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
    }

    private fun determineAuth(){
        lifecycleScope.launch {
            if (mAuth.currentUser?.uid != null){
                navigateToSignin()
            } else {
                navigateToSignup()
            }
        }
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