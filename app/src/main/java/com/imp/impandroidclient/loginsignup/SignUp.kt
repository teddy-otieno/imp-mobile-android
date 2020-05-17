package com.imp.impandroidclient.loginsignup

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.annotations.SerializedName
import com.imp.impandroidclient.R

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
    }
}

