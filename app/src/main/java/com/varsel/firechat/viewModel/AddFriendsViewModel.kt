package com.varsel.firechat.viewModel

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddFriendsViewModel: ViewModel() {
    val shouldRun = MutableLiveData<Boolean>(true)

    fun debounce(onEnd: ()-> Unit): CountDownTimer {
//        shouldRun.value = false
        val timer = object : CountDownTimer(1000, 100){
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                onEnd()
            }
        }.start()

        return timer
    }
}