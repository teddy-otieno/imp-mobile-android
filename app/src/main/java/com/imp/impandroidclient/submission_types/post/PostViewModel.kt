package com.imp.impandroidclient.submission_types.post

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.PostSubmissionRepo
import com.imp.impandroidclient.app_state.repos.TransferStatus
import com.imp.impandroidclient.app_state.repos.data.PostSubmission

/**
 * Note(teddy) variable -> |isExisting| * Will switch to the code path that handles Post submissions patches \
 * Send submission via a PATCH method to server to modify the submissions
 *
 * Note(teddy) Modifications will only be permitted when submission is in a drafting stage(Review)
 *
 * FIXME(teddy): BUG -> When creating a new post submission
 */
class PostViewModel(private var submissionId: Int, campaignId: Int) : ViewModel()
{
    private val submissionRepo = PostSubmissionRepo.getInstance()
    var submission: MutableLiveData<PostSubmission>
    var isExisting: Boolean = true

    init {
        //Create new submission
        if (submissionId < 0)
        {
            submissionId = submissionRepo.createSubmission(campaignId)
            isExisting = false
        }
        submission = submissionRepo.getSubmissionById(submissionId)
    }

    fun submit(): Boolean
    {
        return submission.value?.run {
            if(this.isValid())
            {
                if(isExisting)
                {
                    //Note(teddy) Since all the updates are directly patch to the model
                    //No need to update submission in this method call
                    submissionRepo.patchSubmission(this)
                }
                else
                {
                    submissionRepo.syncSubmission(this)
                }
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