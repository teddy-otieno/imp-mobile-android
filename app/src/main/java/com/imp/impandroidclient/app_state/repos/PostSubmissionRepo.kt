package com.imp.impandroidclient.app_state.repos

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
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
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.IllegalStateException

typealias PostSubmissionsMutableLiveData =
        MutableLiveData<MutableList<MutableLiveData<PostSubmission>>>

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

    fun syncSubmission(submission: PostSubmission, imageUri: Uri) {

        val gson = Gson()
        val requestBody = gson.toJson(submission).toRequestBody(HttpClient.JSON)

        val request = Request.Builder()
            .url("${HttpClient.SERVER_URL}/api/creator/post_submission")
            .header("Authorization", "Bearer ${HttpClient.accessKey}")
            .post(requestBody)
            .build()

        val imageByteArray = getImageByteArray(imageUri)

        repoScope.launch(Dispatchers.IO) {

            try {

                val response = HttpClient.webClient.newCall(request).execute()

                if(response.isSuccessful) {
                    val rawBody = response.body?.string()
                        ?: throw IllegalStateException("EXPECTED RESPONSE BODY")

                    Log.i("POST_SUBMISSION", rawBody)

                    val json = JSONObject(rawBody)
                    val submissionId = json.getInt("pk")

                    if(imageByteArray != null) {

                        val imageRequest = Request.Builder().apply {
                            url("${HttpClient.SERVER_URL}/api/creator/post_submission_image/${submissionId}")
                            header("Authorization", "Bearer ${HttpClient.accessKey}")

                            val filePath = imageUri.path ?: throw IllegalStateException("EXPECTED FILE PATH")
                            val extension = filePath.substring(filePath.lastIndexOf(".")).toLowerCase()

                            if(extension == "png") {
                                post(imageByteArray.toRequestBody(contentType = HttpClient.MEDIA_TYPE_PNG))

                            } else {
                                post(imageByteArray.toRequestBody(contentType = HttpClient.MEDIA_TYPE_JPEG))
                            }

                        }.build()

                        val imageResponse = HttpClient.webClient.newCall(imageRequest).execute()
                        if(!imageResponse.isSuccessful) {
                            TODO("DO SOMETHING")
                        }

                    } else {

                        TODO("IMPLEMENT THIS ERROR PATH")
                    }
                } else {
                    Log.i("POST_SUBMISSION", response.body?.string() ?: "Errorr")

                }

            } catch (e: IOException) {

                Log.w("NETWORK", e.message ?: "FAILED TO SEND SUBMISSION")
            } catch (e: JSONException) {
                Log.w("NETWORK", e.message ?: "")
            }
        }
    }


    private fun compressImageToPNG(bitmap: Bitmap): ByteArray {
        val imagebytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, imagebytes)

        return imagebytes.toByteArray()
    }

    private fun getImageByteArray(content: Uri): ByteArray? {
        val file = File(content.path
            ?: throw IllegalStateException("EXPECTED FILE PATH"))

        val byteArray = ByteArray(file.length().toInt())

        try {
            val inputStream = FileInputStream(file)
            inputStream.read(byteArray)
            return byteArray

        } catch (e: IOException) {
            Log.e("FILE", "Unable to load file")
            e.printStackTrace()

            return null
        }
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
                            ResourceManager.getImageFromCache(it)?.let { bitmap ->

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