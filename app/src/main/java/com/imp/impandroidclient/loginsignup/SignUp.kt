package com.imp.impandroidclient.loginsignup

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.imp.impandroidclient.GlobalApplication
import com.imp.impandroidclient.LoadingPage
import com.imp.impandroidclient.R
import org.json.JSONObject
import java.lang.ref.WeakReference

data class CreatorSignUpInfo(
    @SerializedName("creatorFirstName") var firstName: String? = null,
    @SerializedName("creatorLastName") var lastName: String? = null,
    @SerializedName("creatorGender") var gender: String? = null,
    @SerializedName("creatorDateOfBirth") var dateOfBirth: String? = null,
    @SerializedName("creatorEmailAddress") var emailAddress: String? = null,
    @SerializedName("creatorPhoneNumber") var phoneNumber: String? = null,
    @SerializedName("creatorUsername") var username: String? = null,
    @SerializedName("creatorPassword") var password: String? = null,
    @SerializedName("creatorLocation") var location: String? = null
)

class SignUp : AppCompatActivity(),
    BasicSignUpFragment.OnFragmentInteractionListener,
    ContactInfoFragment.OnFragmentInteractionListener,
    DateOfBirthFragment.OnFragmentInteractionListener,
    SignInCredentialsFragment.SignInCredentialsFragmentListener {

    val signUpData: CreatorSignUpInfo = CreatorSignUpInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_sign_up)


        supportFragmentManager.beginTransaction()
            .add(R.id.sign_up_stage_fragment_container, BasicSignUpFragment())
            .commit()
    }

    override fun onContactInformationRetrieved(creatorEmail: String, creatorPhoneNumber: String) {
        signUpData.emailAddress = creatorEmail
        signUpData.phoneNumber = creatorPhoneNumber
    }

    override fun onDateOfBirthInput(day: String, month: Int, year: String) {
        signUpData.dateOfBirth = "$year-$month-$day"
    }

    override fun getBasicInfo(firstName: String, lastName: String, gender: String) {
        signUpData.firstName = firstName
        signUpData.lastName = lastName
        signUpData.gender = gender
    }

    override fun collectCredentialsAndSignUp(creatorUsername: String, password: String) {
        signUpData.username = creatorUsername
        signUpData.password = password

        //TODO: Move to kotlin coroutines
        SendCreatorSignUpUserData(this).execute(signUpData)
    }
}

class SendCreatorSignUpUserData(activity: Activity) : AsyncTask<CreatorSignUpInfo, Void, Unit>() {
    private val activityRef: WeakReference<Activity> = WeakReference(activity)

    override fun doInBackground(vararg params: CreatorSignUpInfo?) {
        val jsonMessage = Gson().toJson(params[0], CreatorSignUpInfo::class.java)

        val app = activityRef.get()?.application as GlobalApplication

        val jsonRequest = JsonObjectRequest(
            Request.Method.POST,
            GlobalApplication.root_path.plus("/api/accounts/creator-sign-up3"),
            JSONObject(jsonMessage),
            Response.Listener { response ->
                var intent = Intent(activityRef.get(), LoadingPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                val bundle = Bundle().also { bundle ->
                    bundle.putString("username", params[0]?.username)
                    bundle.putString("password", params[0]?.password)
                }

                intent.putExtra("fromSignUpTokens", bundle)

                activityRef.get()?.startActivity(intent)
            },
            Response.ErrorListener
            {
                println("Error Occured While sending data")
                println(it.message)
                println(it.localizedMessage)
                println(it.networkResponse)
                println(it.cause)
            }
        )

        GlobalApplication.httpRequestQueue.add(jsonRequest)
    }
}
