package com.imp.impandroidclient.app_state.repos

import android.graphics.Bitmap
import android.view.Display
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.imp.impandroidclient.app_state.repos.data.PostSubmission
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.IllegalStateException
import kotlin.coroutines.CoroutineContext

typealias PostSubmissionsMutableLiveData = MutableLiveData<MutableList<MutableLiveData<PostSubmission>>>

/**
 *
 */
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

    private val repoScope: CoroutineScope = CoroutineScope(context = Dispatchers.IO)

    private val postSubmissions: PostSubmissionsMutableLiveData = MutableLiveData(mutableListOf())
    val submissions: PostSubmissionsMutableLiveData get() = postSubmissions
    private val networkTransmissionStatus: MutableLiveData<TransferStatus> = MutableLiveData()
    val transferStatus get() = networkTransmissionStatus

    init {
        loadPostSubmissions()
    }

    fun getSubmissionById(id: Int): MutableLiveData<PostSubmission>
    {

        return postSubmissions.value?.find {
            if(it.value != null) {
                it.value!!.id == id
            } else {
                false
            }
        } ?: throw RuntimeException("Undefined behaviour: Submission is supposed to exist")
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
            .url("${HttpClient.SERVER_URL}/api/creator/post_submission")
            .header("Authorization", "Bearer ${HttpClient.accessKey}")
            .post(requestBody)
            .build()

        HttpClient.webClient.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response)
            {
                if(response.isSuccessful)
                {
                    /*
                        * The response returns the id for the send submission
                        * Use the id to submit the image
                        * Note(teddy) Returns the id of the created submission
                        * Use the id to send the submission image
                     */

                    val rawJson = response.body?.string()
                        ?: throw IllegalStateException("Expected Submssion Id inside image data")
                    val json = JSONObject(rawJson)
                    val submissionId = json.getString("id")

                    val body: RequestBody = submission.image?.let {
                        val bytes = compressImageToPNG(it)
                        bytes.toRequestBody(HttpClient.MEDIA_TYPE_PNG, 0, bytes.size)
                    } ?: throw IllegalStateException("Expected submission Image")

                    val imageRequestBody = MultipartBody.Builder()
                        .addFormDataPart("file", DateTime.now().toString(), body) //TODO(teddy) file naming, come up with something better
                        .build()

                    val imageRequest = Request.Builder()
                        .url("${HttpClient.SERVER_URL}/api/creator/post-submission-image/$submissionId")
                        .post(imageRequestBody)
                        .build()

                    HttpClient.webClient.newCall(imageRequest).enqueue(object: Callback {
                        override fun onResponse(call: Call, response: Response) {
                            if(response.isSuccessful)
                                networkTransmissionStatus.postValue(TransferStatus.SUCESSFULL)
                            else
                                networkTransmissionStatus.postValue(TransferStatus.SERVER_ERROR)
                        }

                        override fun onFailure(call: Call, e: IOException) {
                            networkTransmissionStatus.postValue(TransferStatus.FAILED)
                        }
                    })
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


    private fun compressImageToPNG(bitmap: Bitmap): ByteArray
    {
        val imagebytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, imagebytes)

        return imagebytes.toByteArray()
    }
    /**
     * Warning(teddy) This method blocks the main thread and \
     * should not be called inside the main thread.
     *
     */
    private fun loadPostSubmissions() = repoScope.launch(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${HttpClient.SERVER_URL}/api/creator/post_submission")
            .header("Authorization", "Bearer ${HttpClient.accessKey}")
            .get()
            .build()

        try
        {
            val response = HttpClient.webClient.newCall(request).execute()

            if(response.isSuccessful)
            {
                //TODO(teddy) handle this gracefully
                val rawJson: String = response.body?.string()
                    ?: throw IllegalStateException("Expected a body")
                val gson = Gson()

                val submissions = gson.fromJson(rawJson, Array<PostSubmission>::class.java) //TODO(teddy) handle this gracefully

                postSubmissions.postValue(submissions.map {
                    MutableLiveData(it)
                }.toMutableList())
            }

        }
        catch (e: IOException)
        {
            TODO("[Loading PostSubmissions Error] -> Handle this connection error")
        }
    }

    /**
     * @info Read the doc string on the PostViewModel class for \
     * the use of imageChanged flag
     * Launches a coroutine to patch the submission to the server
     */
    fun patchSubmission(submission: PostSubmission, imageChanged: Boolean)
    {
        repoScope.launch(Dispatchers.IO) {
            val gson = Gson()
            val requestBody = gson.toJson(submission).toRequestBody(HttpClient.JSON)

            val request = Request.Builder()
                .url("${HttpClient.SERVER_URL}/api/creator/post_submission")
                .header("Authorization", "Bearer ${HttpClient.accessKey}")
                .patch(requestBody)
                .build()

            try {

                val response = HttpClient.webClient.newCall(request).execute()
                if(response.isSuccessful)
                {

                    if (imageChanged)
                    {

                        val body: RequestBody = submission.image?.let {
                            val bytes = compressImageToPNG(it)
                            bytes.toRequestBody(HttpClient.MEDIA_TYPE_PNG, 0, bytes.size)
                        } ?: throw IllegalStateException("Expected submission Image")

                        val fileName = "submission-${submission.id}.png"

                        /**
                         * BugFix: Added the content-disposition hader
                         *
                         */
                        val imageRequest = Request.Builder()
                            .url("${HttpClient.SERVER_URL}/api/creator/post-submission-image/${submission.id}")
                            .header("Authorization", "Bearer ${HttpClient.accessKey}")
                            .header("Content-Disposition", "attachment;filename=${fileName}")
                            .patch(body)
                            .build()

                        try
                        {
                            val imageResponse = HttpClient.webClient.newCall(imageRequest).execute()
                            if(imageResponse.isSuccessful)
                            {
                                //TODO(Give user feed back)
                            }

                            imageResponse.close()

                        }
                        catch (e: IOException)
                        {

                        }

                    }
                    //Do something
                }
                else
                {
                    TODO("Handle Unsucess in the code path")
                }

                response.close()
            }
            catch (e: IOException)
            {
                TODO("Handle this error within the code path")
            }

        }
    }
}