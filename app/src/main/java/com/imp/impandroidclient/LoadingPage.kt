package com.imp.impandroidclient

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.imp.impandroidclient.dashboards.MainDashboard
import com.imp.impandroidclient.database.AppDatabase
import com.imp.impandroidclient.loginsignup.LoginActivity
import org.json.JSONObject
import java.lang.ref.WeakReference

class LoadingPage : AppCompatActivity() {

    /*
        Construct the database and persistence
        The call the appropriate activity
     */

    val refreshToken: String? = null

    lateinit var database: AppDatabase;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_page)

        val bundle: Bundle? = null //intent.extras?.getBundle("fromSignUpTokens")

        if (bundle == null) {
            loadActivity()
        } else {
            throw RuntimeException("Not implemented")
        }
    }

    fun loadActivity() {
        //TODO: Debug change this
        if (GlobalApplication.refreshToken == null) {
            //Redirect to the signUpLogin Activity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            //TODO: Debug onlyDirect to main dashboard activity
            RetrieveAccessToken(this).execute()
        }
    }
}

class RetrieveAccessToken(activity: Activity) : AsyncTask<Unit, Unit, Unit>() {
    private val activityRef: WeakReference<LoadingPage> = WeakReference(activity as LoadingPage)

    var app = activityRef.get()?.application as GlobalApplication

    override fun doInBackground(vararg params: Unit?) {
        val refreshMessage = JSONObject()
            .put("refresh", GlobalApplication.refreshToken)

        val request = JsonObjectRequest(
            GlobalApplication.root_path.plus("/api/accounts/token/refresh"),
            refreshMessage,
            Response.Listener {
                GlobalApplication.accessToken = it.get("access").toString()
                println("Access Token Received starting activity")

                val intent = Intent(activityRef.get()!!, MainDashboard::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                activityRef.get()?.startActivity(intent)
            },
            Response.ErrorListener {
                //TODO: On error we switch to the loginActivity
            }
        )

        GlobalApplication.httpRequestQueue.add(request)

    }
}