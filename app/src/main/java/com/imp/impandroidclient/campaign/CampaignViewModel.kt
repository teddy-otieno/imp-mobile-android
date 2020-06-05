package com.imp.impandroidclient.campaign

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.CampaignRepository
import com.imp.impandroidclient.app_state.repos.data.CampaignData

class CampaignViewModel(val campaignId: Int) : ViewModel() {
    private val campaignRepo: CampaignRepository = CampaignRepository.getInstance()
    val campaignData: MutableLiveData<CampaignData> = MutableLiveData(campaignRepo.getCampaignOfId(campaignId))

    fun updateCampaign(campaign: CampaignData) {
        campaignRepo.updateCampaign(campaign, campaignId)
        campaignData.postValue(campaignRepo.getCampaignOfId(campaignId))
    }
}