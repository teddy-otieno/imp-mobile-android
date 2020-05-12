package com.imp.impandroidclient.image_submission

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.database.Submission
import com.imp.impandroidclient.app_state.database.SubmissionImage
import java.util.*
import kotlin.collections.ArrayList

data class TempSubmissionData(var postCaption: String? = null,
    var feeRates: Int? = null,
    var timeOfSubmission: Date? = null,
    var images : List<SubmissionImage> = ArrayList()
)

class ImageSubmissionViewModel : ViewModel()
{
    private lateinit var submission: Submission
    private val submissionData: MutableLiveData<TempSubmissionData> = MutableLiveData()

    fun loadSubmission(submissionId: Int)
    {
        if(submissionId < 0) {
            //Create a new submission
            submissionData.postValue(TempSubmissionData())
        } else {
            /*
            this.submission = GlobalApplication.database.submissionQueries().getSubmissionById(submissionId)
            val temporarySubmission = TempSubmissionData(
                submission.postCaption,
                submission.feeRates,
                submission.timeOfSubmission,
                GlobalApplication.database.submissionQueries().getSubmissionImagesForSubmission(this.submission.uid)
            )
            this.submissionData.postValue(temporarySubmission)
             */
        }
    }

    fun getTempSubmissionData() : MutableLiveData<TempSubmissionData> = this.submissionData
}