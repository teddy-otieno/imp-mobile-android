package com.imp.impandroidclient.main_screen.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.CampaignRepository
import com.imp.impandroidclient.app_state.repos.models.Brand
import com.imp.impandroidclient.app_state.repos.models.CampaignData

class HomeViewModel : ViewModel() {

    fun getCampaigns(): MutableLiveData<MutableList<CampaignData>> = CampaignRepository.campaignData
    fun brands() : MutableLiveData<MutableList<Brand>> = CampaignRepository.brands

}