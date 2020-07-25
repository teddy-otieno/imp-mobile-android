package com.imp.impandroidclient.dashboards.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.CampaignRepository
import com.imp.impandroidclient.app_state.repos.data.CampaignData

class HomeViewModel : ViewModel()
{


    fun getCampaigns(): MutableLiveData<MutableList<CampaignData>> = CampaignRepository.campaignData
}