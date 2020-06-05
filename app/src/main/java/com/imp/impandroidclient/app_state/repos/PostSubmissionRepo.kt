package com.imp.impandroidclient.app_state.repos

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.imp.impandroidclient.app_state.repos.data.PostSubmission
import com.imp.impandroidclient.app_state.web_client.HttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

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
    private val networkTransmissionStatus: MutableLiveData<TransferStatus> = MutableLiveData()
    val transferStatus get() = networkTransmissionStatus

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

    fun syncSubmission(submission: PostSubmission)
    {

        val gson = Gson()
        val requestBody = gson.toJson(submission).toRequestBody(HttpClient.JSON)

        val request = Request.Builder()
            .url(HttpClient.SERVER_URL + "/api/creator/post_submission")
            .header("Authorization", "Bearer ${HttpClient.accessKey}")
            .post(requestBody)
            .build()

        HttpClient.webClient.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response)
            {
                if(response.isSuccessful)
                {
                    networkTransmissionStatus.postValue(TransferStatus.SUCESSFULL)
                }
                else
                {
                    networkTransmissionStatus.postValue(TransferStatus.SERVER_ERROR)
                }

            }

            override fun onFailure(call: Call, e: IOException)
            {
                networkTransmissionStatus.value = TransferStatus.FAILED
            }
        })
    }

}