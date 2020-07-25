package com.imp.impandroidclient.app_state.repos

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.imp.impandroidclient.app_state.repos.data.CampaignData
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.*
import okhttp3.Request
import okio.IOException
import org.json.JSONArray

object CampaignRepository {
    val campaignData: MutableLiveData<MutableList<CampaignData>> = MutableLiveData()
    val errorDuringLoading: MutableLiveData<TransferStatus> = MutableLiveData()
    private val scope = CoroutineScope(Dispatchers.IO)


    init {
        scope.launch {
            getNewCampaignsFromServer()
        }
    }

    //Note(teddy) this should be a suspended computation
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

            for(i in 0 until rawCampaigns.length()) {
                val rawCampaign = rawCampaigns[i]
                val campaign: CampaignData = gson.fromJson(rawCampaign.toString(), CampaignData::class.java)
                campaigns.add(campaign)
            }

            campaignData.postValue(campaigns)

        } catch (e: IOException) {
            errorDuringLoading.postValue(TransferStatus.FAILED)
            Log.d("Connection", "${e.message}" )
        }

    }

    fun getCampaignOfId(campaignId: Int): CampaignData {
        return campaignData.value!![campaignId]
    }

    fun updateCampaign(campaign: CampaignData, index: Int) {
        campaignData.value!![index] = campaign
    }


}