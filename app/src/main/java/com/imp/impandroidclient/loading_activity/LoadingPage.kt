package com.imp.impandroidclient.loading_activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.imp.impandroidclient.R
import com.imp.impandroidclient.dashboards.MainDashboard
import com.imp.impandroidclient.loginsignup.LoginActivity

class LoadingPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_page)

        val model: LoadingPageViewModel by viewModels()
        model.getAuth().observe(this, Observer {authenticated: Boolean ->
            if(authenticated) {
                //redirect to homepage
                val intent: Intent = Intent(this, MainDashboard::class.java).apply {
                    this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            } else {
                //redirect to signUp
                val intent: Intent = Intent(this, LoginActivity::class.java).apply {
                    this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        })
    }
}
