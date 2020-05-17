package com.imp.impandroidclient.app_state.repos

import androidx.lifecycle.MutableLiveData
import com.imp.impandroidclient.app_state.repos.data.PostSubmission

class PostSubmissionRepo private constructor(){
    companion object {
        private var instance: PostSubmissionRepo? = null

        fun getInstance(): PostSubmissionRepo {
            return if(instance == null) {
                instance = PostSubmissionRepo()
                instance!!
            } else {
                instance!!
            }

        }
    }

    private val postSubmissions: MutableLiveData<List<PostSubmission>> = MutableLiveData()
    val submissions: MutableLiveData<List<PostSubmission>> get() = postSubmissions
}