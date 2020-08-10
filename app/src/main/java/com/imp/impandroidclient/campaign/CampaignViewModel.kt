package com.imp.impandroidclient.campaign

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.CampaignRepository
import com.imp.impandroidclient.app_state.repos.data.CampaignData

class CampaignViewModel(val campaignId: Int) : ViewModel() {

    val campaignData: MutableLiveData<CampaignData> = MutableLiveData(CampaignRepository.getCampaignOfId(campaignId))
    val dos  get() = CampaignRepository.dosData
    val donts get() = CampaignRepository.dontsData

    fun loadDosAndDonts() = CampaignRepository.loadDosAndDonts(campaignId)
    fun updateCampaign(campaign: CampaignData) {
        CampaignRepository.updateCampaign(campaign, campaignId)
        campaignData.postValue(CampaignRepository.getCampaignOfId(campaignId))
    }
}