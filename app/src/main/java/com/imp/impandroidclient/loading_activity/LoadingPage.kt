package com.imp.impandroidclient.loading_activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.repos.NetworkError
import com.imp.impandroidclient.dashboards.MainDashboard
import com.imp.impandroidclient.loginsignup.login.LoginActivity
import kotlinx.android.synthetic.main.activity_loading_page.*

class LoadingPage : AppCompatActivity() {

    private val model: LoadingPageViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_page)

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

        model.getError().observe(this, Observer {error ->

            when(error) {
                NetworkError.FAILED -> loadingActivityError.setText(R.string.connection_failed)

                else -> Unit
            }
        })
    }
}
