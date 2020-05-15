package com.imp.impandroidclient.app_state.repos

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.imp.impandroidclient.app_state.web_client.HttpClient
import com.imp.impandroidclient.dashboards.ui.data_classes.CampaignData
import com.imp.impandroidclient.helpers.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.Request
import okio.IOException
import org.json.JSONArray

class CampaignRepository
{
    val campaignData: MutableLiveData<MutableList<CampaignData>> = MutableLiveData()

    val errorDuringLoading: MutableLiveData<NetworkError> = MutableLiveData()

    init {
        GlobalScope.launch(context = Dispatchers.IO) {
            getNewCampaignsFromServer()
        }
    }

    companion object {
        @Volatile
        private var instance: CampaignRepository? = null

        fun getInstance(): CampaignRepository {
            if(instance == null) {
                instance = CampaignRepository()
                return instance!!
            }
            return instance!!
        }
    }

    private fun getNewCampaignsFromServer() {

        val request = Request.Builder()
            .url(HttpClient.SERVER_URL + "/api/creator/campaigns")
            .get()
            .addHeader("Authorization", "Bearer " + HttpClient.accessKey)
            .build()

        try {
            val response = HttpClient.webClient.newCall(request).execute()

            val rawJson = response.body!!.string()

            val rawCampaigns = JSONArray(rawJson)

            val gson = Gson()
            val campaigns: MutableList<CampaignData> = mutableListOf()
            for(i in 0 until  rawCampaigns.length()) {
                val rawCampaign = rawCampaigns[i]

                val campaign: CampaignData = gson.fromJson(rawCampaign.toString(), CampaignData::class.java)

                campaign.coverImageFuture = GlobalScope.async { loadImage(campaign.cover_image) }
                campaign.brand.brandImageFuture = GlobalScope.async { loadImage(campaign.brand.brand_image) }

                campaigns.add(campaign)
            }

            campaignData.postValue(campaigns)

        } catch (e: IOException) {
            println(e)
        }

    }

    fun getCampaignOfId(campaignId: Int): CampaignData {
        return campaignData.value!![campaignId]
    }

    fun updateCampaign(campaign: CampaignData, index: Int) {
        campaignData.value!![index] = campaign
    }
}