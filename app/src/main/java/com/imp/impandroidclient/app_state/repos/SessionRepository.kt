package com.imp.impandroidclient.app_state.repos

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.imp.impandroidclient.app_state.database.AppDatabase
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class SessionRepository private constructor() {
    private val db = AppDatabase.getDatabaseInstance()

    val isAuthenticated: MutableLiveData<Boolean> = MutableLiveData()
    val errorOnAuth: MutableLiveData<NetworkError> = MutableLiveData()

    companion object {
        @Volatile
        private var instance: SessionRepository? = null

        fun getInstance(): SessionRepository {
            if(instance == null) {
                instance = SessionRepository()
                return instance!!
            }

            return instance!!
        }

    }
    init {
        authenticate()
    }

    private fun authenticate() {

        GlobalScope.launch {
            val refreshKey = db.sessionDao().getCurrentCredentials()

            if(refreshKey == null) {
                isAuthenticated.postValue(false)
            } else {

                /*
                val refreshMessage = JSONObject().put("refresh", refreshKey)
                val old_request = JsonObjectRequest(
                    Request.Method.POST,
                    HttpClient.SERVER_URL + "/api/accounts/token/refresh",
                    refreshMessage,
                    Response.Listener {
                        isAuthenticated.postValue(true)

                        HttpClient.accessKey = it.getString("access")
                        HttpClient.refreshKey = refreshKey
                    },
                    Response.ErrorListener {
                        isAuthenticated.postValue(false)
                    }
                )
                HttpClient.sendRequest(request)
                */

                val refreshMessage = JSONObject().put("refresh", refreshKey)
                val request = Request.Builder()
                    .url(HttpClient.SERVER_URL + "/api/accounts/token/refresh")
                    .post(refreshMessage.toString().toRequestBody(HttpClient.JSON))
                    .build()

                HttpClient.webClient.newCall(request).enqueue(object: Callback{
                    override fun onResponse(call: Call, response: Response) {
                        if(response.isSuccessful) {
                            isAuthenticated.postValue(true)
                            val rawJson = response.body!!.string()
                            println("json ${rawJson}")
                            val json = JSONObject(rawJson)
                            HttpClient.accessKey = json.getString("access")
                            HttpClient.refreshKey = refreshKey
                        } else {
                            isAuthenticated.postValue(false)

                            println("Error ${response.body.toString()}")
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        errorOnAuth.postValue(NetworkError.FAILED)
                        Log.e("Connection", e.message ?: "Connection Error")
                    }
                })
            }
        }
    }

    fun login(username: String, password: String) {
        GlobalScope.launch {
            val userCredentialsObject = JSONObject()
                .put("username", username)
                .put("password", password)

            /*
            val old_request = JsonObjectRequest(
                Request.Method.POST,
                HttpClient.SERVER_URL + "/api/accounts/token",
                userCredentialsObject,
                Response.Listener {
                    isAuthenticated.postValue(true)

                    HttpClient.accessKey = it.getString("access")
                    HttpClient.refreshKey = it.getString("refresh")

                    println("refresh ${HttpClient.refreshKey}")
                    GlobalScope.launch {
                        db.sessionDao().addRefreshToken(it.getString("refresh"))
                        println("Reached this")
                    }
                },
                Response.ErrorListener {
                    errorOnAuth.postValue(true)
                }
            )

            */
            val request = Request.Builder()
                .url(HttpClient.SERVER_URL + "/api/accounts/token")
                .post(userCredentialsObject.toString().toRequestBody(HttpClient.JSON))
                .build()

            HttpClient.webClient.newCall(request).enqueue(object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    TODO("Handle")
                }

                override fun onFailure(call: Call, e: IOException) {
                }
            })

        }
    }

}