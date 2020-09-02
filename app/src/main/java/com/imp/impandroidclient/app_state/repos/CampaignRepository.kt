package com.imp.impandroidclient.app_state.repos

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.imp.impandroidclient.app_state.repos.models.*
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.*
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import java.lang.IllegalStateException
import java.util.*

const val CONNECTION_FAILED_MESSAGE = "Unable to connect with the server"
const val SERVER_ERROR = "Request was not processed"
const val EMPTY_RESPONSE = "EMPTY RESPONSE BODY"

private enum class CacheItemNames {
    HASH_TAG,
    DOS,
    DONTS,
    MOODBOARDS
};


object CampaignRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val alreadyLoadedCache: EnumMap<CacheItemNames, MutableList<Int>> = EnumMap(CacheItemNames::class.java)

    val campaignData: MutableLiveData<MutableList<CampaignData>> by lazy {
        MutableLiveData( mutableListOf<CampaignData>())
    }
    val brands: MutableLiveData<MutableList<Brand>> by lazy {
        MutableLiveData(mutableListOf<Brand>())
    }
    val hashTags: MutableLiveData<List<HashTag>> by lazy {
        MutableLiveData<List<HashTag>>()
    }
    val dosData: MutableLiveData<List<GenericCampaignItem>> by lazy {
        MutableLiveData<List<GenericCampaignItem>>()
    }
    val dontsData: MutableLiveData<List<GenericCampaignItem>> by lazy {
        MutableLiveData<List<GenericCampaignItem>>()
    }
    val moodBoards: MutableLiveData<List<MoodBoard>> by lazy {
        MutableLiveData<List<MoodBoard>>()
    }

    val errorDuringLoading: MutableLiveData<TransferStatus> by lazy {
        MutableLiveData<TransferStatus>()
    }



    init {
        getNewCampaignsFromServer()


        alreadyLoadedCache.apply {
            put(CacheItemNames.HASH_TAG, mutableListOf())
            put(CacheItemNames.DOS, mutableListOf())
            put(CacheItemNames.DONTS, mutableListOf())
            put(CacheItemNames.MOODBOARDS, mutableListOf())
        }
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
                        loadBrand(campaign.brand)
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

    fun getBrand(brandInt: Int): Brand {
        return brands.value?.let {
            it.find { brand ->
                brand.id == brandInt
            }
        } ?: throw IllegalStateException("PRETTY SURE BRAND SHOULD EXIST")
    }

    suspend fun loadBrand(brandId: Int) = withContext(Dispatchers.IO) {
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

    suspend fun loadHashTags(campaignId: Int) = withContext(Dispatchers.IO) {

        val hashTagCache = alreadyLoadedCache[CacheItemNames.HASH_TAG]
            ?: throw IllegalStateException("ITEM INSIDE THE HASHTAG WAS ALREADY LOADED")

        val campaign = hashTagCache.find {
            it == campaignId
        }

        if(campaign == null) {

            val request = Request.Builder()
                .url("${HttpClient.SERVER_URL}/api/company/hashtags/${campaignId}")
                .get()
                .addHeader("Authorization", "Bearer ${HttpClient.accessKey}")
                .build()

            try {
                val response = HttpClient.webClient.newCall(request).execute()

                if(response.isSuccessful) {

                    val rawJson = response.body?.string()
                        ?: throw IllegalStateException("RESPONSE BODY IS EMPTY")

                    val gson = Gson()

                    val loadedHashtags = gson.fromJson<Array<HashTag>>(rawJson, Array<HashTag>::class.java)
                    hashTags.postValue(loadedHashtags.asList())

                    hashTagCache.add(campaignId)
                }

            } catch (exception: IOException) {
                Log.w("NETWORK", "Failed to establish server connection")

            }

        }
    }

    fun loadDosAndDonts(campaignId: Int) {

        val dosCache = alreadyLoadedCache[CacheItemNames.DOS]
            ?: throw IllegalStateException("ITEM INSIDE THE HASHTAG WAS ALREADY LOADED")
        val dontsCache = alreadyLoadedCache[CacheItemNames.DONTS]
            ?: throw IllegalStateException("ITEM INSIDE THE HASHTAG WAS ALREADY LOADED")

        val campaignForDos = dosCache.find { it == campaignId }
        val campaignForDonts = dontsCache.find { it == campaignId }

        val processResponse
                = { response: Response, store: MutableLiveData<List<GenericCampaignItem>> ->

            val rawJSonBody = response.body?.string()
                ?: throw IllegalStateException(EMPTY_RESPONSE)

            val data: Array<GenericCampaignItem> = HttpClient.gson.fromJson(
                rawJSonBody,
                Array<GenericCampaignItem>::class.java
            )

            store.postValue(data.toList())

        }

        if(campaignForDonts == null) {

            scope.launch(Dispatchers.IO) {

                val request = Request.Builder().apply {
                    url("${HttpClient.SERVER_URL}/api/company/dos/${campaignId}")
                    addHeader("Authorization", "Bearer ${HttpClient.accessKey}")
                    get()
                }.build()

                try {

                    val response = HttpClient.webClient.newCall(request).execute()

                    if(response.isSuccessful) {
                        processResponse(response, dosData)

                    } else {
                        Log.d("SERVER ERROR", "Unable to process the server request ")
                    }
                } catch (e: IOException) {
                    Log.d(
                        "SERVER CONNECTION",
                        e.message ?: CONNECTION_FAILED_MESSAGE
                    )
                }

            }
        }

        if(campaignForDos == null) {
            scope.launch(Dispatchers.IO) {

                val request = Request.Builder().apply {
                    url("${HttpClient.SERVER_URL}/api/company/donts/${campaignId}")
                    addHeader("Authorization", "Bearer ${HttpClient.accessKey}")
                    get()
                }.build()

                try {

                    val response = HttpClient.webClient.newCall(request).execute()

                    if(response.isSuccessful) {
                        processResponse(response, dontsData)

                    } else {
                        Log.d( "SERVER ERROR", SERVER_ERROR )
                    }

                    response.close()

                } catch (e: IOException) {

                    Log.d("SERVER CONNECTION", e.message ?: CONNECTION_FAILED_MESSAGE)

                }

            }
        }

    }

    suspend fun loadMoodBoards(campaignId: Int) = withContext(Dispatchers.IO) {

        val moodBoardCache = alreadyLoadedCache[CacheItemNames.MOODBOARDS]
            ?: throw IllegalStateException("ITEM WAS EXPECTED")

        val campaign = moodBoardCache.find { it == campaignId }

        if(campaign == null) {

            val request = Request.Builder().apply {

                url("${HttpClient.SERVER_URL}/api/company/mood_boards/${campaignId}")
                addHeader("Authorization", "Bearer ${HttpClient.accessKey}")
                get()

            }.build()

            try {
                val response = HttpClient.webClient.newCall(request).execute()
                val rawJson = response.body?.string()
                    ?: throw IllegalStateException(EMPTY_RESPONSE)

                val moodBoardsArray = HttpClient.gson.fromJson(rawJson, Array<MoodBoard>::class.java)

                moodBoards.postValue(moodBoardsArray.toList())

                response.close()

            } catch(e: IOException) {

                Log.d("SERVER CONNECTION", e.message ?: CONNECTION_FAILED_MESSAGE)

            }
        }
    }
}