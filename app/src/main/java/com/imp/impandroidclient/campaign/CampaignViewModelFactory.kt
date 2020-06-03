package com.imp.impandroidclient.campaign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CampaignViewModelFactory(private val campaignId: Int) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(CampaignViewModel::class.java)) {
            return CampaignViewModel(campaignId) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}