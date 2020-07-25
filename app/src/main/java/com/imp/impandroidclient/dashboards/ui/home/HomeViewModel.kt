package com.imp.impandroidclient.dashboards.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.CampaignRepository
import com.imp.impandroidclient.app_state.repos.data.Brand
import com.imp.impandroidclient.app_state.repos.data.CampaignData
import java.lang.IllegalStateException

class HomeViewModel : ViewModel() {

    fun getCampaigns(): MutableLiveData<MutableList<CampaignData>> = CampaignRepository.campaignData

    fun brands() : MutableLiveData<MutableList<Brand>> = CampaignRepository.brands

}