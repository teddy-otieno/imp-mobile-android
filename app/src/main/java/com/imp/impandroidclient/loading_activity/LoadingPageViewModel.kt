package com.imp.impandroidclient.loading_activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.SessionRepository
import com.imp.impandroidclient.app_state.repos.TransferStatus

class LoadingPageViewModel : ViewModel(){

    private val sessionRepository: SessionRepository = SessionRepository.getInstance()

    fun getAuth() : MutableLiveData<Boolean> = sessionRepository.isAuthenticated

    fun getError(): MutableLiveData<TransferStatus> = sessionRepository.errorOnAuth
}