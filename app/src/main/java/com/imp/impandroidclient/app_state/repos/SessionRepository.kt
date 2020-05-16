package com.imp.impandroidclient.app_state.repos

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.imp.impandroidclient.app_state.database.AppDatabase
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
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

        GlobalScope.launch(Dispatchers.IO) {
            val refreshKey = db.sessionDao().getCurrentCredentials()

            Log.d("Sqlite", "refresh ${refreshKey}")
            if(refreshKey == null) {
                isAuthenticated.postValue(false)
            } else {

                val refreshMessage = JSONObject().put("refresh", refreshKey)
                val request = Request.Builder()
                    .url(HttpClient.SERVER_URL + "/api/accounts/token/refresh")
                    .post(refreshMessage.toString().toRequestBody(HttpClient.JSON))
                    .build()

                Log.d("Http", refreshMessage.toString())

                HttpClient.webClient.newCall(request).enqueue(object: Callback{
                    override fun onResponse(call: Call, response: Response) {
                        if(response.isSuccessful) {
                            val rawJson = response.body!!.string()
                            println("json ${rawJson}")
                            val json = JSONObject(rawJson)
                            HttpClient.accessKey = json.getString("access")
                            HttpClient.refreshKey = refreshKey
                            isAuthenticated.postValue(true)
                        } else {
                            Log.d("HTTP", "${response.body?.string()}")
                            isAuthenticated.postValue(false)
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
        GlobalScope.launch(Dispatchers.IO) {
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
                    val body = response.body

                    if(body == null) {
                        errorOnAuth.postValue(NetworkError.FAILED)
                        isAuthenticated.postValue(false)
                    } else {
                        val rawJson = body.string()
                        val json = JSONObject(rawJson)

                        try {
                            Log.d("HTTP", "${json.toString()}")
                            //Incase a broken respone is returned
                            HttpClient.accessKey = json.getString("access")
                            HttpClient.refreshKey = json.getString("refresh")

                            GlobalScope.launch(Dispatchers.IO) {
                                db.sessionDao().addRefreshToken(HttpClient.refreshKey)
                                println("Reached this")
                            }

                            isAuthenticated.postValue(true)
                        } catch(e: JSONException) {
                            errorOnAuth.postValue(NetworkError.BAD_RESPONSE)
                            isAuthenticated.postValue(false)
                        }
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                }
            })

        }
    }

}