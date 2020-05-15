package com.imp.impandroidclient.application

import android.app.Application
import android.content.Context
import com.imp.impandroidclient.app_state.Cache
import com.imp.impandroidclient.app_state.database.AppDatabase

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
    }

    override fun onLowMemory() {
        super.onLowMemory()

        //Empty all caches
        Cache.clearCaches()
    }
}

