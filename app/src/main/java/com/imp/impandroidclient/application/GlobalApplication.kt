package com.imp.impandroidclient.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.database.AppDatabase
import com.imp.impandroidclient.app_state.repos.FileSystemMedia
import net.danlew.android.joda.JodaTimeAndroid


const val CHANNEL_ID = "imp-activity"
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
        FileSystemMedia.application = this
    }

    override fun onLowMemory() {
        super.onLowMemory()

        ResourceManager.clear()
    }

}

