package com.varsel.firechat.presentation.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateGroupViewModel : ViewModel() {
    val hasBtnBeenClicked = MutableLiveData<Boolean>(false)
}