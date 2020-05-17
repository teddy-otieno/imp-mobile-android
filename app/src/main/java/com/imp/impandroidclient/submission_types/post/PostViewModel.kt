package com.imp.impandroidclient.submission_types.post

import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.PostSubmissionRepo

class PostViewModel : ViewModel()
{
    private val submissionRepo = PostSubmissionRepo.getInstance()
}