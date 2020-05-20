package com.imp.impandroidclient.loginsignup.signup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class SignUpViewModel: ViewModel() {

    val fName: MutableLiveData<String> = MutableLiveData()
    val lName: MutableLiveData<String> = MutableLiveData()
    val dateOfBirth: MutableLiveData<Date> = MutableLiveData()

}