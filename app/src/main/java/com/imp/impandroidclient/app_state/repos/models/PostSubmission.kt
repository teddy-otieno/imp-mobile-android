package com.imp.impandroidclient.app_state.repos.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class PostSubmission(
    @SerializedName("id")               val id: Int = 0,
    @SerializedName("campaign")         val campaignId: Int,
    @SerializedName("caption")          var postCaption: String? = null,
    @SerializedName("fee")              var fee: Int? = null,
    @SerializedName("note")             var note: String? = null,
    @SerializedName("submission_time")  var timeOfSubmission: Date? = null,
    @SerializedName("status")           var status: String? = null,
    @SerializedName("image")            var image_url: String? = null
){
    fun isValid(): Boolean {

        return !(postCaption.isNullOrEmpty() or
                (fee == null) or
                (note.isNullOrEmpty()))
    }
}