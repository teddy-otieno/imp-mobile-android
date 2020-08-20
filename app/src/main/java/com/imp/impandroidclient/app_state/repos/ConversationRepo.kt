package com.imp.impandroidclient.app_state.repos

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.imp.impandroidclient.app_state.repos.data.Brand
import com.imp.impandroidclient.app_state.repos.data.Conversation
import com.imp.impandroidclient.app_state.repos.data.Message
import com.imp.impandroidclient.app_state.web_client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.io.IOException
import java.lang.IllegalStateException
import java.util.*
import kotlin.collections.HashMap

typealias ConversationID = Int

object ConversationRepo {

    val messages: HashMap<ConversationID, MutableLiveData<List<Message>>> by lazy {
        hashMapOf<ConversationID, MutableLiveData<List<Message>>>()
    }

    val conversations: MutableLiveData<List<Conversation>> by lazy {
        MutableLiveData<List<Conversation>>()
    }

    private val mNewConversationMessages by lazy {
        hashMapOf<ConversationID, MutableLiveData<Queue<Message>>>()
    }


    fun getNewConversationMessage(conversationID: ConversationID) : MutableLiveData<Queue<Message>>? {
        return mNewConversationMessages[conversationID]
    }

    suspend fun loadConversations() = withContext(Dispatchers.IO){

        val request = Request.Builder().apply {
            url("${HttpClient.SERVER_URL}/api/messaging/conversations")
            addHeader("Authorization", "Bearer ${HttpClient.accessKey}")
            get()
        }.build()

        try {
            val response = HttpClient.webClient.newCall(request).execute()

            if(response.isSuccessful) {
                val rawJson = response.body?.string()
                    ?: throw IllegalStateException("Expected response body")

                val rawConversations: Array<Conversation> = HttpClient.gson.fromJson(
                    rawJson,
                    Array<Conversation>::class.java
                )

                for (conversation in rawConversations) {
                    try {

                        val loadedMessages = syncLoadMessages(conversation.id)
                        messages[conversation.id] = MutableLiveData(loadedMessages)

                    } catch (e: IOException) {
                        Log.d("NETWORK", CONNECTION_FAILED_MESSAGE)

                    } catch (e: IllegalStateException) {
                        Log.d("SERVER", SERVER_ERROR)
                    }
                }

                conversations.postValue(rawConversations.toList())
            } else {

                Log.d("SERVER", SERVER_ERROR)
            }
        } catch (e: IOException) {
            Log.d("NETWORK", CONNECTION_FAILED_MESSAGE)
        }

    }


    suspend fun loadMessages(conversationId: Int) = withContext(Dispatchers.IO) {
        try {
            val loadedMessages = syncLoadMessages(conversationId)

            messages[conversationId] = MutableLiveData(loadedMessages)

        } catch(e: IOException) {
            Log.d("CONNECTION", CONNECTION_FAILED_MESSAGE)
        }
    }

    private fun syncLoadMessages(conversationId: Int): List<Message> {
        val request = Request.Builder().apply {
            url("${HttpClient.SERVER_URL}/api/messaging/get_messages/${conversationId}")
            addHeader("Authorization", "Bearer ${HttpClient.accessKey}")
            get()
        }.build()

        val response = HttpClient.webClient.newCall(request).execute()

        if(response.isSuccessful) {
            val rawMessages = response.body?.string()
                ?: throw IllegalStateException("EXPECTED RESPONSE BODY")

            Log.d("RESPONSE", rawMessages)
            val messages: Array<Message> = HttpClient.gson.fromJson(
                rawMessages,
                Array<Message>::class.java
            )

            return messages.toList()

        } else {
            throw IllegalStateException("RESPONSE IS EMPTY")
        }

    }

    fun getBrand(subId: Int) : Brand {
        val submission = PostSubmissionRepo.getSubmissionById(subId)
        val campaignId = submission.campaignId

        val campaign = CampaignRepository.getCampaignOfId(campaignId)
        val brand = CampaignRepository.getBrand(campaign.brand)

        return brand
    }

    suspend fun sendMessage(message: Message) = withContext(Dispatchers.IO){

        try {
            val messageJson = HttpClient.gson.toJson(message).toRequestBody(HttpClient.JSON)

            val request = Request.Builder().apply {
                url("${HttpClient.SERVER_URL}/api/messaging/send_message")
                addHeader("Authorization", "Bearer ${HttpClient.accessKey}")
                post(messageJson)
            }.build()

            val response = HttpClient.webClient.newCall(request).execute()

            if(response.isSuccessful) {

                val rawJson = response.body?.string() ?: throw IllegalStateException("")
                val jsonObject = JSONObject(rawJson)

                val pk = jsonObject.getInt("pk")
                if(pk == 0) throw IllegalStateException("PRIMARY KEY IS NOT SUPPOSED TO BE NULL")


                val data = messages[message.conversationId]

                data?.value?.let {
                    val messages = it.toMutableList()
                    message.id = pk
                    messages.add(message)

                    data.postValue(messages)
                }

            } else {
                Log.d("SERVER_ERROR", response.code.toString())
            }

        } catch (e: IOException) {
            Log.d("CONNECTION_FAILED", CONNECTION_FAILED_MESSAGE)
        }

    }

    fun receiveNewMessage(message: Message) {
        val newMessages = mNewConversationMessages[message.conversationId]

        newMessages?.let {
            if(it.value == null) {
                it.value = LinkedList()

            } else {
                val messageQueue = it.value
                messageQueue?.add(message)
                newMessages.postValue(messageQueue)

            }
        }
    }
}