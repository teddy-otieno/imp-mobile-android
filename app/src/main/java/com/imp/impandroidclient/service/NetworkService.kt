package com.imp.impandroidclient.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.repos.ConversationRepo
import com.imp.impandroidclient.app_state.repos.models.SocketMessageEvent
import com.imp.impandroidclient.app_state.web_client.HttpClient
import com.imp.impandroidclient.application.CHANNEL_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener


const val MSG_LISTEN_CONVERSATION = 0x001

class NetworkService : Service() {

    private val mScope = CoroutineScope(Dispatchers.IO)

    private lateinit var serviceLooper: Looper
    private lateinit var serviceHandler: ServiceHandler
    private lateinit var messenger: Messenger

    private inner class ServiceHandler(looper: Looper): Handler(looper) {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            when(msg.what) {
                MSG_LISTEN_CONVERSATION -> {
                    val request = Request.Builder().apply {
                        url("${HttpClient.SERVER_WEBSOCKET_URL}/messaging_subscription/${msg.arg1}/?token=${HttpClient.accessKey}")
                    }.build()

                    //TODO(teddy) Maybe send connections
                    HttpClient.webClient.newWebSocket(request, object: WebSocketListener() {
                        override fun onOpen(webSocket: WebSocket, response: Response) {
                            Log.d("WEB_SOCKET", "CONNECTION OPENED ${msg.arg1}")
                        }

                        override fun onMessage(webSocket: WebSocket, text: String) {
                            Log.d("WEB_SOCKET", "Message received ${text}")

                            val message = HttpClient.gson.fromJson<SocketMessageEvent>(text, SocketMessageEvent::class.java)
                            if(message.event == "MESSAGE") {

                                sendNotificationMessage(message.message)
                                ConversationRepo.receiveNewMessage(message.message)
                            }
                        }
                    })

                }
            }
        }
    }

    fun sendNotificationMessage(message: com.imp.impandroidclient.app_state.repos.models.Message) {

        val notificationBuilder = NotificationCompat.Builder(this@NetworkService, "MESSAGE")
            .setContentTitle(message.conversationId.toString())
            .setSmallIcon(R.drawable.ic_bullet_icon)
            .setContentText(message.message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT


        with(NotificationManagerCompat.from(this@NetworkService)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                createNotificationChannel(channel)
                notificationBuilder.setChannelId(CHANNEL_ID)
                Log.d("NOTIFICATION", "New notification created")
            }


            notify(MSG_LISTEN_CONVERSATION, notificationBuilder.build())
        }
    }

    override fun onCreate() {
        super.onCreate()

        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            //thread for work
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        messenger = Messenger(serviceHandler)
        return messenger.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        serviceHandler.obtainMessage().also { msg ->
            msg.arg1 = startId
            serviceHandler.sendMessage(msg)
        }

        return START_STICKY
    }
}


