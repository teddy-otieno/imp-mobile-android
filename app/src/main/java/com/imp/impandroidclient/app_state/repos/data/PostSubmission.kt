package com.imp.impandroidclient.app_state.repos.data

import android.graphics.Bitmap
import java.util.*

data class PostSubmission(
    val id: Int,
    val campaignId: Int,
    var postCaption: String? = null,
    var feeRate: Int? = null,
    var notes: String? = null,
    val timeOfSubmission: Date? = null,
    val submissionImage: Bitmap? = null,
    val status: SubmissionStatus? = null
)