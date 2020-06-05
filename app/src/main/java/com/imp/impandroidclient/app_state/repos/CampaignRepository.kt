package com.imp.impandroidclient.app_state.repos

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.imp.impandroidclient.app_state.Cache
import com.imp.impandroidclient.app_state.repos.data.CampaignData
import com.imp.impandroidclient.app_state.web_client.HttpClient
import com.imp.impandroidclient.helpers.loadImage
import kotlinx.coroutines.*
import okhttp3.Request
import okio.IOException
import org.json.JSONArray

class CampaignRepository
{
    val campaignData: MutableLiveData<MutableList<CampaignData>> = MutableLiveData()
    val errorDuringLoading: MutableLiveData<TransferStatus> = MutableLiveData()
    private val scope = CoroutineScope(Dispatchers.IO)


    init {
        scope.launch {
            getNewCampaignsFromServer()
        }
    }

    companion object
    {
        @Volatile
        private var instance: CampaignRepository? = null

        fun getInstance(): CampaignRepository
        {

            return if(instance == null) {
                instance = CampaignRepository()
                instance!!
            } else {
                instance!!
            }
        }

        suspend fun getImage(imageFuture: Deferred<Bitmap?>?, imageName: String): Bitmap?
        {
            val coverImageCached = Cache.getImageFromMemCache(imageName)

            return if(coverImageCached == null) {
                val coverImage = imageFuture?.await()
                if(coverImage != null) {
                    Cache.addImageToMemCache(imageName, coverImage)
                    coverImage
                } else {
                    null
                }

            } else {
                coverImageCached
            }
        }
    }

    //Note(teddy) this should be a suspended computation
    private fun getNewCampaignsFromServer()
    {

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
                campaign.coverImageFuture = scope.async { loadImage(campaign.cover_image) }
                campaign.brand.brandImageFuture = scope.async { loadImage(campaign.brand.brand_image) }
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