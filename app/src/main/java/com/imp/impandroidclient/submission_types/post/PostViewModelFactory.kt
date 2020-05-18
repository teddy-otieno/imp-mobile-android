package com.imp.impandroidclient.submission_types.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PostViewModelFactory(private val postSubmissionId: Int, private val campaignId: Int) : ViewModelProvider.Factory
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T
    {
        if(modelClass.isAssignableFrom(PostViewModel::class.java))
        {
            return PostViewModel(postSubmissionId, campaignId) as T
        }

        throw IllegalArgumentException("Unknown View Model Class")
    }
}