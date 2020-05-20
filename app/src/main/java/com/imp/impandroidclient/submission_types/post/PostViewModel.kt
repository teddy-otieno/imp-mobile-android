package com.imp.impandroidclient.submission_types.post

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.TransferStatus
import com.imp.impandroidclient.app_state.repos.PostSubmissionRepo
import com.imp.impandroidclient.app_state.repos.data.PostSubmission

class PostViewModel(private var submissionId: Int, campaignId: Int) : ViewModel()
{
    private val submissionRepo = PostSubmissionRepo.getInstance()
    var submission: MutableLiveData<PostSubmission>

    init
    {
        assert(campaignId >= 0)
        if(submissionId < 0)
        {
            //Create new submission
            submissionId = submissionRepo.createSubmission(campaignId)
        }
        submission = submissionRepo.getSubmissionById(submissionId)
    }

    fun submit()
    {
        assert(submission.value != null)
        submissionRepo.syncSubmission(submission.value!!)
    }

    fun transferStatus(): MutableLiveData<TransferStatus> = submissionRepo.transferStatus
}