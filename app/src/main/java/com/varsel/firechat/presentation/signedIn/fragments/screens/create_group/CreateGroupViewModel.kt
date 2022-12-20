package com.varsel.firechat.presentation.signedIn.fragments.screens.create_group

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateGroupViewModel : ViewModel() {
    val hasBtnBeenClicked = MutableLiveData<Boolean>(false)
}