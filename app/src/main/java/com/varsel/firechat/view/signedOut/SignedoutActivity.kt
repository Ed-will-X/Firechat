package com.varsel.firechat.view.signedOut

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.varsel.firechat.R
import com.varsel.firechat.databinding.ActivitySignedinBinding
import com.varsel.firechat.databinding.ActivitySignedoutBinding

class SignedoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignedoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignedoutBinding.inflate(layoutInflater)
        val view = binding.root

//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        setContentView(view)
    }
}