package com.imp.impandroidclient.loading_activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.SessionRepository
import com.imp.impandroidclient.app_state.repos.TransferStatus

class LoadingPageViewModel : ViewModel(){


    fun getAuth() : MutableLiveData<Boolean> = SessionRepository.isAuthenticated

    fun getError(): MutableLiveData<TransferStatus> = SessionRepository.errorOnAuth
}