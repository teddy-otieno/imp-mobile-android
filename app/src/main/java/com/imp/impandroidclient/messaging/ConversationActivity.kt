package com.imp.impandroidclient.messaging

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.imp.impandroidclient.CAMPAIGN_ID
import com.imp.impandroidclient.CONVERSATION_ID
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.repos.ConversationID
import com.imp.impandroidclient.app_state.repos.ConversationRepo
import com.imp.impandroidclient.app_state.repos.models.Message
import kotlinx.android.synthetic.main.activity_conversation.*
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.util.*
import kotlin.properties.Delegates

class ConversationActivity : AppCompatActivity() {

    private val viewModel: ConversationActivityViewModel by viewModels()
    private var conversationId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        val bundle = intent.extras ?: throw IllegalStateException("EXPECTED A BUNDLE")

        val tempId = bundle.getInt(CONVERSATION_ID)
        conversationId = if(tempId != 0) tempId else run {
            throw IllegalStateException("EXPECTED A $CAMPAIGN_ID")
        }

        setUpObservers()
        setUpListeners()

    }

    private fun setUpObservers() {

        val ref = this
        viewModel.getMessages(conversationId).observe(this, Observer { messages ->

            chat.apply {

                //FIXME(teddy) Move this to somewhere better
                val sorted = messages.sortedBy {
                    it.time
                }
                adapter = ChatAdapter(ref, sorted.reversed())
                layoutManager = LinearLayoutManager(
                    ref,
                    LinearLayoutManager.VERTICAL,
                    true
                )
            }

        })

    }

    private fun setUpListeners() {
        send_message.setOnClickListener {

            if(message_input.text.isNotBlank() || message_input.text.isNotEmpty()) {

                val message = Message(
                    0,
                    Date(),
                    "PENDING",
                    message_input.text.toString(),
                    "CRE",
                    conversationId
                )

                viewModel.sendMessage(message)

                message_input.text.clear()
            }
        }
    }
}


class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(message: Message, parentActivity: ConversationActivity) {
        val brand = ConversationRepo.getBrand(message.conversationId)

        val brandNameview = itemView.findViewById<TextView>(R.id.brand_name)
        brandNameview.text = brand.title

        val messageView: TextView = itemView.findViewById(R.id.message)
        messageView.text = message.message

        val chatBubble: MaterialCardView = itemView.findViewById(R.id.message_bubble)

        val layoutParams = chatBubble.layoutParams as RelativeLayout.LayoutParams
        if(message.sender == "COM") {

            if(layoutParams.getRule(RelativeLayout.ALIGN_PARENT_LEFT) <= 0 )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)

        } else {

            if(layoutParams.getRule(RelativeLayout.ALIGN_PARENT_RIGHT) <= 0 )
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        }
    }
}

class ChatAdapter(
    private val parentActivity: ConversationActivity,
    private val messages: List<Message>
) : RecyclerView.Adapter<MessageViewHolder>() {

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position], parentActivity)
    }

    override fun getItemCount(): Int = messages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {

        val view = parentActivity.layoutInflater.inflate(
            R.layout.item_message_bubble,
            parent,
            false
        )

        return MessageViewHolder(view)
    }
}

class ConversationActivityViewModel: ViewModel() {

    fun getMessages(convoId: ConversationID) : MutableLiveData<List<Message>> {
        return ConversationRepo.messages[convoId] ?: throw IllegalStateException("NOT SUPPOSED TO HAPPEND")
    }

    fun sendMessage(message: Message) {


        viewModelScope.launch{

            ConversationRepo.sendMessage(message)
        }
    }
}
