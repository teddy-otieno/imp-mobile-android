package com.imp.impandroidclient.dashboards.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.CampaignRepository
import com.imp.impandroidclient.dashboards.ui.data_classes.CampaignData

class HomeViewModel : ViewModel()
{

    private val campaignRepository: CampaignRepository = CampaignRepository.getInstance()

    fun getCampaigns(): MutableLiveData<MutableList<CampaignData>> = campaignRepository.campaignData
}