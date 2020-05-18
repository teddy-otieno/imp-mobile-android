package com.imp.impandroidclient.app_state.repos

import androidx.lifecycle.MutableLiveData
import com.imp.impandroidclient.app_state.repos.data.PostSubmission

typealias PostSubmissionsMutableLiveData = MutableLiveData<MutableList<MutableLiveData<PostSubmission>>>

class PostSubmissionRepo private constructor(){
    companion object
    {
        private var instance: PostSubmissionRepo? = null

        fun getInstance(): PostSubmissionRepo
        {
            return if(instance == null)
            {
                instance = PostSubmissionRepo()
                instance!!
            }
            else
            {
                instance!!
            }

        }
    }

    private val postSubmissions: PostSubmissionsMutableLiveData = MutableLiveData(mutableListOf())
    val submissions: PostSubmissionsMutableLiveData get() = postSubmissions

    fun getSubmissionById(id: Int): MutableLiveData<PostSubmission>
    {
        val submission = postSubmissions.value?.find {
            if(it.value != null) {
                it.value!!.id == id
            } else {
                false
            }
        }

        if(submission != null)
        {
            return submission
        } else {
            throw RuntimeException("Undefined behaviour: Submission is supposed to exist")
        }
    }

    fun createSubmission(campaignId: Int): Int
    {
        return if(postSubmissions.value!!.isEmpty())
        {
            val post = PostSubmission(1, campaignId)
            postSubmissions.value = mutableListOf(MutableLiveData(post))
            1
        }
        else
        {
            val newSubmissionId = postSubmissions.value!!.last().value!!.id
            val newSubmission = PostSubmission(newSubmissionId, campaignId)

            val newSubmissions = postSubmissions.value!!
            newSubmissions.add(MutableLiveData(newSubmission))
            postSubmissions.value = newSubmissions
            newSubmissionId
        }
    }
}