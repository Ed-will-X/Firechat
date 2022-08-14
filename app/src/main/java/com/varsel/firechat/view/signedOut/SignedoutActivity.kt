package com.varsel.firechat.view.signedOut

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActivitySignedoutBinding
import kotlinx.coroutines.launch

class SignedoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignedoutBinding
    lateinit var datastore: DataStore<Preferences>
    lateinit var mAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // datastore
        datastore = createDataStore("current_user")

        // firebase
        mAuth = FirebaseAuth.getInstance()


        mDbRef = FirebaseDatabase.getInstance().getReference()

        binding = ActivitySignedoutBinding.inflate(layoutInflater)
        val view = binding.root


        // status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        setContentView(view)
    }
}