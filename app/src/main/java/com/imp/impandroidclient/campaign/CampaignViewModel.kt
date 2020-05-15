package com.imp.impandroidclient.campaign

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.CampaignRepository
import com.imp.impandroidclient.dashboards.ui.data_classes.CampaignData

class CampaignViewModel(val campaignId: Int) : ViewModel() {
    private val campaignRepo: CampaignRepository = CampaignRepository.getInstance()
    private val campaignData: MutableLiveData<CampaignData> = MutableLiveData(campaignRepo.getCampaignOfId(campaignId))

    fun getCampaign(): MutableLiveData<CampaignData> = campaignData
    fun updateCampaign(campaign: CampaignData) {
        campaignRepo.updateCampaign(campaign, campaignId)
        campaignData.postValue(campaignRepo.getCampaignOfId(campaignId))
    }
}