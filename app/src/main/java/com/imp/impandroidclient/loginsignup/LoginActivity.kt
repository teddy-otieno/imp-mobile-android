package com.imp.impandroidclient.loginsignup

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.imp.impandroidclient.GlobalApplication
import com.imp.impandroidclient.R
import com.imp.impandroidclient.dashboards.MainDashboard
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_activity_username.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                login_activity_username.setBackgroundResource(R.drawable.input_field)
            }
        }
        login_activity_password.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                login_activity_password.setBackgroundResource(R.drawable.input_field)
            }
        }

        launch_sign_up_activity.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)

            startActivity(intent)
        }

        login_button.setOnClickListener {
            val context = this;
            lifecycleScope.launch {
                val username = login_activity_username.text.toString();
                val password = login_activity_password.text.toString();

                if ((username.isBlank() or username.isEmpty()) or (password.isBlank() or password.isEmpty())) {
                    if (username.isBlank() or username.isEmpty()) {
                        login_activity_username.setBackgroundResource(R.drawable.input_field_error)
                    }

                    if (password.isBlank() or password.isEmpty()) {
                        login_activity_password.setBackgroundResource(R.drawable.input_field_error)
                    }
                } else {
                    authenticateLogin(context, username, password, Response.ErrorListener {
                        println(it.message)
                        greetings_message.run {
                            this.setText(R.string.login_error)
                            this.setTextColor(resources.getColor(R.color.colorError))
                            this.setTypeface(this.typeface, Typeface.BOLD)
                        }
                    })
                }
            }
        }
    }
}

fun authenticateLogin(
    context: Activity,
    username: String,
    password: String,
    onErrorCallback: Response.ErrorListener
) {

    val app = context.application as GlobalApplication
    val credentials = JSONObject()
        .put("username", username)
        .put("password", password)

    val request = JsonObjectRequest(
        GlobalApplication.root_path.plus("/api/accounts/token"),
        credentials,
        Response.Listener {
            app.receiveKeyTokens(it.get("access").toString(), it.get("refresh").toString())
            println("access token received, starting Main activity")
            val intent = Intent(context, MainDashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        },
        onErrorCallback
    )

    GlobalApplication.httpRequestQueue.add(request);
}