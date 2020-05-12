package com.imp.impandroidclient.app_state.repos

import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.imp.impandroidclient.app_state.database.AppDatabase
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class SessionRepository private constructor() {
    private val db = AppDatabase.getDatabaseInstance()

    val isAuthenticated: MutableLiveData<Boolean> = MutableLiveData()
    val errorOnAuth: MutableLiveData<Boolean> = MutableLiveData(false)

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

                val refreshMessage = JSONObject().put("refresh", refreshKey)
                val request = JsonObjectRequest(
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
            }
        }
    }

    fun login(username: String, password: String) {
        GlobalScope.launch {
            val userCredentialsObject = JSONObject()
                .put("username", username)
                .put("password", password)

            val request = JsonObjectRequest(
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

            HttpClient.sendRequest(request)
        }
    }

}