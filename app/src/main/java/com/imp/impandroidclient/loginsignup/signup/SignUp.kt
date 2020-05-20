package com.imp.impandroidclient.loginsignup.signup

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.annotations.SerializedName
import com.imp.impandroidclient.R


class SignUp : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportFragmentManager.beginTransaction()
            .add(R.id.sign_up_stage_fragment_container,
                BasicSignUpFragment()
            ).commit()
    }
}

