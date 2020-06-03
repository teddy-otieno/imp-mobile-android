package com.imp.impandroidclient.app_state.repos.data

import com.google.gson.annotations.SerializedName

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

