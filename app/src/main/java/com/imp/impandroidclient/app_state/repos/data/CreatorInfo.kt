package com.imp.impandroidclient.app_state.repos.data

import com.google.gson.annotations.SerializedName

data class CreatorSignUpInfo(
    @SerializedName("first_name")       var firstName: String? = null,
    @SerializedName("last_name")        var lastName: String? = null,
    @SerializedName("gender")           var gender: String? = null,
    @SerializedName("date_of_birth")    var dateOfBirth: String? = null,
    @SerializedName("email")            var emailAddress: String? = null,
    @SerializedName("phone_number")     var phoneNumber: String? = null,
    @SerializedName("username")         var username: String? = null,
    @SerializedName("password")         var password: String? = null,
    @SerializedName("location")         var location: String? = null
)

