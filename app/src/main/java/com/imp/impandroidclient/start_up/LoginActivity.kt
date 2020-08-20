package com.imp.impandroidclient.start_up

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.repos.SessionRepository
import com.imp.impandroidclient.app_state.repos.TransferStatus
import com.imp.impandroidclient.main_screen.MainDashboard
import com.imp.impandroidclient.start_up.signup.SignUp
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {


    private val loginViewModel: LoginActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setUpListeners()
        updateState()

        loginViewModel.isAuthenticated().observe(this, Observer {authed ->
            if(authed) {
                val intent: Intent = Intent(this, MainDashboard::class.java).apply {
                    this.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
        })

        loginViewModel.errorOnAuth().observe(this, Observer { occurred ->

            when(occurred){
                TransferStatus.FAILED -> greetings_message.run {
                    this.setText(R.string.login_error)
                    this.setTextColor(resources.getColor(R.color.colorError))
                    this.setTypeface(this.typeface, Typeface.BOLD)
                }

                else -> Unit
            }
        })
    }

    private fun setUpListeners() {
        login_activity_username.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                login_activity_username.setBackgroundResource(R.drawable.input_field)
            } else {
                loginViewModel.username = login_activity_username.text.toString()
            }
        }

        login_activity_password.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                login_activity_password.setBackgroundResource(R.drawable.input_field)
            } else {
                loginViewModel.password = login_activity_password.text.toString()
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
                    loginViewModel.authenticate(username, password)
                }
            }
        }
    }

    private fun updateState() {
        login_activity_username.setText(loginViewModel.username)
        login_activity_password.setText(loginViewModel.password)
    }
}

class LoginActivityViewModel : ViewModel(){

    var username: String = ""
    var password: String = ""

    fun isAuthenticated(): MutableLiveData<Boolean> = SessionRepository.isAuthenticated
    fun errorOnAuth(): MutableLiveData<TransferStatus> = SessionRepository.errorOnAuth

    fun authenticate(username: String, password: String) {
        SessionRepository.login(username, password)
    }
}

