package com.imp.impandroidclient.dashboards.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.google.gson.Gson
import com.imp.impandroidclient.GlobalApplication
import com.imp.impandroidclient.dashboards.ui.data_classes.CampaignData
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val loadedCampaignData: MutableLiveData<ArrayList<CampaignData>> by lazy {
        MutableLiveData<ArrayList<CampaignData>>().also {
            loadCampaigns()
        }
    }

    fun getCampaigns(): LiveData<ArrayList<CampaignData>> {
        return this.loadedCampaignData
    }

    private fun loadCampaigns() {
        viewModelScope.launch {
            getNewCampaignsFromServer()
        }
    }

    private fun getNewCampaignsFromServer() {
        if (GlobalApplication.accessToken == null) {
            return;
        }

        val url = GlobalApplication.root_path.plus("/api/creator/campaigns")
        val request = object : JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener {

                if (it.length() == 0) {
                    return@Listener;
                }

                val campaigns: ArrayList<CampaignData> = ArrayList();

                for (i in 0 until it.length()) {
                    val jsonObject = it.getJSONObject(i)
                    val deserializedCampaignObject = Gson().fromJson<CampaignData>(
                        jsonObject.toString(),
                        CampaignData::class.java
                    )

                    campaigns.add(deserializedCampaignObject)
                    println("Reached here")
                }

                loadedCampaignData.postValue(campaigns)
            },
            Response.ErrorListener {}) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>();
                headers["Authorization"] = "Bearer " + GlobalApplication.accessToken
                return headers
            }
        }

        GlobalApplication.httpRequestQueue.add(request)
        Unit
    }
}