package com.varsel.firechat.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.varsel.firechat.R
import com.varsel.firechat.view.signedIn.SignedinActivity
import com.varsel.firechat.viewModel.FirebaseViewModel

class LifecycleUtils {
    companion object {
        fun observeInternetStatus(activity: SignedinActivity, fragment: Fragment, connectedCallback: ()-> Unit, disconnectedCallback: ()-> Unit){
            activity.firebaseViewModel.isConnectedToDatabase.observe(fragment.viewLifecycleOwner, Observer {
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

        fun observeLiveData(liveData: MutableLiveData<Any>, fragment: Fragment, results: (results: Any)-> Unit){
            liveData.observe(fragment.viewLifecycleOwner, Observer {
                results(it)
            })
        }
    }
}