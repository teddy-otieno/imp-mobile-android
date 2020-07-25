package com.imp.impandroidclient.application

import android.app.Application
import android.content.Context
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.database.AppDatabase
import net.danlew.android.joda.JodaTimeAndroid

class GlobalApplication : Application() {
    //Initialize the database

    companion object{
        private lateinit var context: Context
        fun getApplicationContext(): Context = context
    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        AppDatabase.createDatabase(this)
        JodaTimeAndroid.init(this)
    }

    override fun onLowMemory() {
        super.onLowMemory()

        ResourceManager.clear()
    }
}

