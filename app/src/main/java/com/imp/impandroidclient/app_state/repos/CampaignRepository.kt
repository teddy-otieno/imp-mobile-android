package com.imp.impandroidclient.app_state.repos

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.imp.impandroidclient.app_state.repos.data.Brand
import com.imp.impandroidclient.app_state.repos.data.CampaignData
import com.imp.impandroidclient.app_state.repos.data.HashTag
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.*
import okhttp3.Request
import okio.IOException
import org.json.JSONArray
import java.lang.IllegalStateException

object CampaignRepository {

    val campaignData: MutableLiveData<MutableList<CampaignData>> by lazy {
        MutableLiveData( mutableListOf<CampaignData>())
    }
    val brands: MutableLiveData<MutableList<Brand>> by lazy {
        MutableLiveData(mutableListOf<Brand>())
    }
    val hashTags: MutableLiveData<List<HashTag>> by lazy {
        MutableLiveData<List<HashTag>>()
    }
    val errorDuringLoading: MutableLiveData<TransferStatus> by lazy {
        MutableLiveData<TransferStatus>()
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val hashTagForCampaignsAlreadyLoaded: MutableList<Int> = mutableListOf()

    init {
        getNewCampaignsFromServer()
    }

    //Note(teddy) this should be a suspended computation
    private fun getNewCampaignsFromServer() {

        scope.launch {

            val request = Request.Builder()
                .url(HttpClient.SERVER_URL + "/api/creator/campaigns")
                .get()
                .addHeader("Authorization", "Bearer " + HttpClient.accessKey)
                .build()

            try {
                val response = HttpClient.webClient.newCall(request).execute()

                if(response.code == 200)  {
                    val rawJson = response.body?.string() ?: throw IllegalStateException("Response Body is Empty")
                    val rawCampaigns = JSONArray(rawJson)
                    val gson = Gson()
                    val campaigns: MutableList<CampaignData> = mutableListOf()

                    for(i in 0 until rawCampaigns.length()) {
                        val rawCampaign = rawCampaigns[i]
                        val campaign: CampaignData = gson.fromJson(rawCampaign.toString(), CampaignData::class.java)
                        campaigns.add(campaign)
                        getBrand(campaign.brand)
                    }

                    campaignData.postValue(campaigns)
                } else {
                    errorDuringLoading.postValue(TransferStatus.FAILED)
                    Log.w("NETWORK", "Response Body is empty skipping")
                }

            } catch (e: IOException) {
                errorDuringLoading.postValue(TransferStatus.FAILED)
                Log.w("Connection", "${e.message}" )
            }
        }

    }

    fun getCampaignOfId(campaignId: Int): CampaignData {
        return campaignData.value?.find {
            it.id == campaignId
        } ?: throw IllegalStateException("EXPECTED A VALID CAMPAIGN ID")
    }

    fun updateCampaign(campaign: CampaignData, index: Int) {
        campaignData.value!![index] = campaign
    }

    fun getBrand(brandId: Int) {
        scope.launch(Dispatchers.IO) {

            val request = Request.Builder()
                .url("${HttpClient.SERVER_URL}/api/company/brand/${brandId}")
                .get()
                .addHeader("Authorization", "Bearer ${HttpClient.accessKey}")
                .build()

            try {
                val response = HttpClient.webClient.newCall(request).execute()

                if(response.code == 200) {
                    val rawJson = response.body?.string() ?: throw IllegalStateException("RESPONSE BODY IS EMPTY")
                    val gson = Gson()

                    val brand = gson.fromJson<Brand>(rawJson, Brand::class.java)

                    brands.value?.let {

                        val temp = it
                        temp.add(brand)

                        brands.postValue(temp)
                    }
                } else {
                    Log.w("NETWORK", "Failed to get brands from server")
                }

            } catch (e: IOException) {
                Log.w("NETWORK", "Failed to establish connection with server")
            }
        }
    }

    fun loadHashTags(campaignId: Int) {

        val campaign = hashTagForCampaignsAlreadyLoaded.find {
            it == campaignId
        }

        if(campaign == null) {

            val request = Request.Builder()
                .url("${HttpClient.SERVER_URL}/api/company/hashtags/${campaignId}")
                .get()
                .addHeader("Authorization", "Bearer ${HttpClient.accessKey}")
                .build()

            scope.launch(Dispatchers.IO) {
                try {
                    val response = HttpClient.webClient.newCall(request).execute()

                    if(response.isSuccessful) {

                        val rawJson = response.body?.string()
                            ?: throw IllegalStateException("RESPONSE BODY IS EMPTY")

                        val gson = Gson()

                        val loadedHashtags = gson.fromJson<Array<HashTag>>(rawJson, Array<HashTag>::class.java)
                        hashTags.postValue(loadedHashtags.asList())

                        hashTagForCampaignsAlreadyLoaded.add(campaignId)
                    }

                } catch (exception: IOException) {
                    Log.w("NETWORK", "Failed to establish server connection")

                }
            }

        }
    }
}