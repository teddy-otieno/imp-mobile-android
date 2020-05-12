package com.imp.impandroidclient.campaign

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.helpers.CampaignParcelable

class CampaignViewModel() : ViewModel() {
    val campaignData: MutableLiveData<CampaignParcelable> = MutableLiveData()

    fun setData(campaign: CampaignParcelable) {
        if (campaignData.value == null) {
            //Set the data once
            campaignData.postValue(campaign)
        }
    }
}