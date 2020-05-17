package com.imp.impandroidclient.loginsignup.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.NetworkError
import com.imp.impandroidclient.app_state.repos.SessionRepository

class LoginActivityViewModel : ViewModel(){

    private val sessionRepo: SessionRepository = SessionRepository.getInstance()
    var username: String = ""
    var password: String = ""

    fun isAuthenticated(): MutableLiveData<Boolean> = sessionRepo.isAuthenticated
    fun errorOnAuth(): MutableLiveData<NetworkError> = sessionRepo.errorOnAuth

    fun authenticate(username: String, password: String) {
        sessionRepo.login(username, password)
    }
}
