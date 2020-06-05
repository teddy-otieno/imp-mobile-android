package com.imp.impandroidclient.submission_types.post

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.PostSubmissionRepo
import com.imp.impandroidclient.app_state.repos.TransferStatus
import com.imp.impandroidclient.app_state.repos.data.PostSubmission

class PostViewModel(private var submissionId: Int, campaignId: Int) : ViewModel()
{
    private val submissionRepo = PostSubmissionRepo.getInstance()
    var submission: MutableLiveData<PostSubmission>
    val image: MutableLiveData<Bitmap> = MutableLiveData()

    init {
        //Create new submission
        if (submissionId < 0)
            submissionId = submissionRepo.createSubmission(campaignId)
        submission = submissionRepo.getSubmissionById(submissionId)
    }

    fun submit(): Boolean
    {
        return submission.value?.run {
            if(this.isValid())
            {
                submissionRepo.syncSubmission(this)
                true
            }
            else
            {
                false
            }
        } ?: false
    }

    fun transferStatus(): MutableLiveData<TransferStatus> = submissionRepo.transferStatus
}