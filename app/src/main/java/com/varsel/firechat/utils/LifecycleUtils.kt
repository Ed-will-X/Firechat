package com.varsel.firechat.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.varsel.firechat.viewModel.FirebaseViewModel

class LifecycleUtils {
    companion object {
        fun observeInternetStatus(firebaseViewModel: FirebaseViewModel, fragment: Fragment, connectedCallback: ()-> Unit, disconnectedCallback: ()-> Unit){
            firebaseViewModel.isConnectedToDatabase.observe(fragment.viewLifecycleOwner, Observer {
                if(it){
                    connectedCallback()
                } else {
                    disconnectedCallback()
                }
            })
        }

        fun showToast(context: Context, text: String){
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }
}