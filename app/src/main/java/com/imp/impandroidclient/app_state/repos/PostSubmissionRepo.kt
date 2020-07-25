package com.imp.impandroidclient.app_state.repos

import android.graphics.Bitmap
import android.view.Display
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.repos.data.PostSubmission
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.*
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
object PostSubmissionRepo {

    private val repoScope: CoroutineScope = CoroutineScope(context = Dispatchers.IO)
    private val postSubmissions: PostSubmissionsMutableLiveData = MutableLiveData(mutableListOf())
    private val networkTransmissionStatus: MutableLiveData<TransferStatus> = MutableLiveData()

    val submissions: PostSubmissionsMutableLiveData get() = postSubmissions
    val transferStatus get() = networkTransmissionStatus

    init {
        loadPostSubmissions()
    }

    /**
     * Reset the instance when a view model requests an instance
     */
    private fun reset_status() {
        networkTransmissionStatus.value = TransferStatus.NOTHING
    }

    fun getSubmissionById(id: Int): MutableLiveData<PostSubmission> {

        return postSubmissions.value?.find {
            if(it.value != null) {
                it.value!!.id == id
            } else {
                false
            }
        } ?: throw RuntimeException("Undefined behaviour: Submission is supposed to exist")
    }

    fun createSubmission(campaignId: Int): Int {

        return if(postSubmissions.value!!.isEmpty()) {
            val post = PostSubmission(1, campaignId)
            postSubmissions.value = mutableListOf(MutableLiveData(post))
            1

        } else {
            val newSubmissionId = postSubmissions.value!!.last().value!!.id
            val newSubmission = PostSubmission(newSubmissionId, campaignId)

            val newSubmissions = postSubmissions.value!!
            newSubmissions.add(MutableLiveData(newSubmission))
            postSubmissions.value = newSubmissions
            newSubmissionId
        }
    }

    fun syncSubmission(submission: PostSubmission) {

        val gson = Gson()
        val requestBody = gson.toJson(submission).toRequestBody(HttpClient.JSON)

        val request = Request.Builder()
            .url("${HttpClient.SERVER_URL}/api/creator/post_submission")
            .header("Authorization", "Bearer ${HttpClient.accessKey}")
            .post(requestBody)
            .build()

        HttpClient.webClient.newCall(request).enqueue(object: Callback {

            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful) {
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

                    val body: RequestBody = submission.image_url?.let {
                        /**
                         * TODO(teddy) handle cache empty sceneario
                         */
                        ResourceManager.getImage(it)?.let { bitmap ->
                            val bytes = compressImageToPNG(bitmap)
                            bytes.toRequestBody(HttpClient.MEDIA_TYPE_PNG, 0, bytes.size)
                        } ?: throw IllegalStateException("Image Not found in the Cache")

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
                                networkTransmissionStatus.postValue(TransferStatus.FAILED)
                        }

                        override fun onFailure(call: Call, e: IOException) {
                            networkTransmissionStatus.postValue(TransferStatus.FAILED)
                        }
                    })
                    networkTransmissionStatus.postValue(TransferStatus.SUCESSFULL)

                } else {
                    networkTransmissionStatus.postValue(TransferStatus.FAILED)

                }

            }

            override fun onFailure(call: Call, e: IOException) {
                networkTransmissionStatus.value = TransferStatus.FAILED
            }
        })
    }


    private fun compressImageToPNG(bitmap: Bitmap): ByteArray {
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

        try {
            val response = HttpClient.webClient.newCall(request).execute()

            if(response.isSuccessful) {
                //TODO(teddy) handle this gracefully
                val rawJson: String = response.body?.string()
                    ?: throw IllegalStateException("Expected a body")
                val gson = Gson()

                val submissions = gson.fromJson(rawJson, Array<PostSubmission>::class.java) //TODO(teddy) handle this gracefully

                postSubmissions.postValue(submissions.map {
                    MutableLiveData(it)
                }.toMutableList())
            }

        } catch (e: IOException) {
            TODO("[Loading PostSubmissions Error] -> Handle this connection error")
        }
    }

    /**
     * @info Read the doc string on the PostViewModel class for \
     * the use of imageChanged flag
     * Launches a coroutine to patch the submission to the server
     */
    fun patchSubmission(submission: PostSubmission, imageChanged: Boolean) {

        repoScope.launch(Dispatchers.IO) {
            val gson = Gson()
            val requestBody = gson.toJson(submission).toRequestBody(HttpClient.JSON)

            val request = Request.Builder()
                .url("${HttpClient.SERVER_URL}/api/creator/post_submission")
                .header("Authorization", "Bearer ${HttpClient.accessKey}")
                .patch(requestBody)
                .build()

            try {

                networkTransmissionStatus.postValue(TransferStatus.INPROGRESS)
                val response = HttpClient.webClient.newCall(request).execute()
                if(response.isSuccessful) {

                    if (imageChanged) {

                        val body: RequestBody = submission.image_url?.let {
                            /**
                             * TODO(teddy) handle cache empty sceneario
                             * TODO(teddy) Refactor code duplication
                             */
                            ResourceManager.getImage(it)?.let { bitmap ->

                                val bytes = compressImageToPNG(bitmap)
                                bytes.toRequestBody(HttpClient.MEDIA_TYPE_PNG, 0, bytes.size)

                            } ?: throw IllegalStateException("Image Not found in the Cache")

                        } ?: throw IllegalStateException("Expected submission Image")

                        val fileName = "submission-${submission.id}.png"

                        val imageRequest = Request.Builder()
                            .url("${HttpClient.SERVER_URL}/api/creator/post-submission-image/${submission.id}")
                            .header("Authorization", "Bearer ${HttpClient.accessKey}")
                            .header("Content-Disposition", "attachment;filename=${fileName}")
                            .patch(body)
                            .build()

                        try {
                            val imageResponse = HttpClient.webClient.newCall(imageRequest).execute()

                            if(imageResponse.isSuccessful) {
                                //TODO(Give user feed back)
                                networkTransmissionStatus.postValue(TransferStatus.SUCESSFULL)
                            }

                            imageResponse.close()

                        } catch (e: IOException) {
                            TODO()
                        }

                    }

                    networkTransmissionStatus.postValue(TransferStatus.SUCESSFULL)

                } else {
                    TODO("Handle Unsucess in the code path")
                }

                response.close()

            } catch (e: IOException) {
                TODO("Handle this error within the code path")
            }

        }
    }
}