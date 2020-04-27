package com.imp.impandroidclient

import android.app.Application
import android.os.AsyncTask
import androidx.room.Room
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.imp.impandroidclient.database.AppDatabase

class GlobalApplication : Application() {
    //Initialize the database

    companion object {
        const val root_path: String = "http://192.168.0.15:8000"
        lateinit var httpRequestQueue: RequestQueue
        lateinit var database: AppDatabase
        var accessToken: String? = null
        var refreshToken: String? = null
    }


    override fun onCreate() {
        super.onCreate()

        httpRequestQueue = Volley.newRequestQueue(this)

        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "imp-internal"
        )
            .build()

        GetRefreshToken().execute()
    }

    fun receiveKeyTokens(accessToken: String, refreshToken: String) {
        GlobalApplication.accessToken = accessToken
        GlobalApplication.refreshToken = refreshToken
        AddRefreshToken().execute(refreshToken)
    }
}

class AddRefreshToken : AsyncTask<String, Unit, Unit>() {

    override fun doInBackground(vararg params: String?) {
        GlobalApplication.database.sessionDao().addRefreshToken(params[0] ?: "")
    }
}

class GetRefreshToken : AsyncTask<Unit, Unit, Unit>() {

    override fun doInBackground(vararg params: Unit?) {
        GlobalApplication.refreshToken =
            GlobalApplication.database.sessionDao().getCurrentCredentials()
    }
}
