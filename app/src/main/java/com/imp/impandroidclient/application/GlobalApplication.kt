package com.imp.impandroidclient.application

import android.app.Application
import com.imp.impandroidclient.app_state.database.AppDatabase
import com.imp.impandroidclient.app_state.web_client.HttpClient

class GlobalApplication : Application() {
    //Initialize the database

    override fun onCreate() {
        super.onCreate()

        AppDatabase.createDatabase(this)
        HttpClient.initializeRequestQueue(this)
    }
}

