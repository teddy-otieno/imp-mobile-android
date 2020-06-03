package com.imp.impandroidclient.loginsignup.signup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.imp.impandroidclient.app_state.repos.SessionRepository
import com.imp.impandroidclient.app_state.repos.data.CreatorSignUpInfo
import com.imp.impandroidclient.app_state.web_client.HttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SignUpViewModel: ViewModel()
{
    val fName: MutableLiveData<String> = MutableLiveData()
    val lName: MutableLiveData<String> = MutableLiveData()
    val dateOfBirth: MutableLiveData<Date> = MutableLiveData()
    val gender: MutableLiveData<String> by lazy { MutableLiveData<String>()}

    val email: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val phoneNumber: MutableLiveData<String> by lazy { MutableLiveData<String>()}
    val username: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val password: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val agreement: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    fun submit()
    {
        assert(password.value != null)

        val user_info = CreatorSignUpInfo(
            firstName = fName.value,
            lastName = lName.value,
            dateOfBirth = SimpleDateFormat("yyyy-MM-dd").format(dateOfBirth.value!!),
            gender = gender.value,
            emailAddress = email.value,
            phoneNumber = phoneNumber.value,
            username = username.value,
            password = password.value
        )

        val gson = Gson()

        val request = Request.Builder()
            .url(HttpClient.SERVER_URL + "/api/accounts/creator-sign-up")
            .post(gson.toJson(user_info).toString().toRequestBody(HttpClient.JSON))
            .build()

        HttpClient.webClient.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response)
            {
                if(response.isSuccessful)
                {
                    assert(user_info.username != null)
                    assert(user_info.password != null)
                    SessionRepository.getInstance().login(user_info.username!!,user_info.password!!)
                }
            }

            override fun onFailure(call: Call, e: IOException)
            {
                TODO("Not yet implemented")
            }
        })
    }
}