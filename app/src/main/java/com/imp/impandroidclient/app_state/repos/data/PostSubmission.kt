package com.imp.impandroidclient.app_state.repos.data

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Deferred
import java.util.*

data class PostSubmission(
    @SerializedName("id")               val id: Int,
    @SerializedName("campaign")         val campaignId: Int,
    @SerializedName("caption")          var postCaption: String? = null,
    @SerializedName("fee")              var fee: Int? = null,
    @SerializedName("note")             var note: String? = null,
    @SerializedName("submission_time")  var timeOfSubmission: Date? = null,
    @SerializedName("status")           var status: SubmissionStatus? = null,
    @SerializedName("image")            var image_url: String? = null
){
    fun isValid(): Boolean
    {
        return !(postCaption.isNullOrEmpty() or
                (fee == null) or
                (note.isNullOrEmpty()))
    }
}