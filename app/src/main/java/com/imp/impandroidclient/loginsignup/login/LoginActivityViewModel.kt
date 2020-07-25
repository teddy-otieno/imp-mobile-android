package com.imp.impandroidclient.loginsignup.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.SessionRepository
import com.imp.impandroidclient.app_state.repos.TransferStatus

class LoginActivityViewModel : ViewModel(){

    var username: String = ""
    var password: String = ""

    fun isAuthenticated(): MutableLiveData<Boolean> = SessionRepository.isAuthenticated
    fun errorOnAuth(): MutableLiveData<TransferStatus> = SessionRepository.errorOnAuth

    fun authenticate(username: String, password: String) {
        SessionRepository.login(username, password)
    }
}
