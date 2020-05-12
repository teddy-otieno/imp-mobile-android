package com.imp.impandroidclient.loading_activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.SessionRepository

class LoadingPageViewModel : ViewModel(){

    private val sessionRepository: SessionRepository = SessionRepository.getInstance()

    fun getAuth() : MutableLiveData<Boolean> = sessionRepository.isAuthenticated
}