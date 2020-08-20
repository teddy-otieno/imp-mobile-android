package com.imp.impandroidclient.app_state.repos

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.imp.impandroidclient.app_state.repos.data.CreatorAccount
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException
import java.lang.IllegalStateException

object UserAccount {

    val account: MutableLiveData<CreatorAccount> by lazy {
        MutableLiveData<CreatorAccount>()
    }

    suspend fun loadUser() = withContext(Dispatchers.IO) {

        val request = Request.Builder().apply {
            url("${HttpClient.SERVER_URL}/api/account/creator-info/0")
            addHeader("Authorization", "Bearer ${HttpClient.accessKey}")
            get()
        }.build()

        try {
            val response = HttpClient.webClient.newCall(request).execute()

            if(response.isSuccessful) {

                val rawJson = response.body?.string()
                    ?: throw IllegalStateException("EXPECTED A FUNCTION BODY")

                val parsedAccount: CreatorAccount = HttpClient.gson.fromJson(rawJson, CreatorAccount::class.java)

            } else {
                Log.d("SERVER", SERVER_ERROR)
            }

        } catch(e: IOException) {
            Log.d("CONNECTION", CONNECTION_FAILED_MESSAGE)
        }
    }
}