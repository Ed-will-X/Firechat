package com.varsel.firechat.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.varsel.firechat.R
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.view.signedOut.SignedoutActivity
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
            if (mAuth.currentUser?.uid.toString().length > 2){
                navigateToSignin()
            } else {
                navigateToSignup()
            }
        }
    }

    private fun navigateToSignin(){
        val intent = Intent(this, SignedinActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToSignup(){
        val intent = Intent(this, SignedoutActivity::class.java)
        startActivity(intent)
        finish()
    }
}