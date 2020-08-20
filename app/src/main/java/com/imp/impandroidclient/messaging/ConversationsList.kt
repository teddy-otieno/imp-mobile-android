package com.imp.impandroidclient.messaging

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imp.impandroidclient.CONVERSATION_ID
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.repos.ConversationID
import com.imp.impandroidclient.app_state.repos.ConversationRepo
import com.imp.impandroidclient.app_state.repos.data.Conversation
import com.imp.impandroidclient.app_state.repos.data.Message
import kotlinx.android.synthetic.main.activity_conversations_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class ConversationsList : AppCompatActivity() {

    private val viewModel: ConversationViewModel by viewModels()
    val activityModel: ConversationViewModel get() = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations_list)

        val ref = this
        convo_list.apply {
            layoutManager = LinearLayoutManager(
                ref,
                LinearLayoutManager.VERTICAL,
                false
            )
        }

        viewModel.conversations.observe(this, Observer {
            convo_list.adapter = ConversationListAdapter(this, it)

            for(convo in it) {
                viewModel.loadMessages(convo.id)
            }
        })

        viewModel.loadConversations()
    }
}

class ConversationViewHolder(view: View): RecyclerView.ViewHolder(view) {

    fun bind(conversation: Conversation, parentActivity: ConversationsList) {
        val brandName: TextView = itemView.findViewById(R.id.brand_name)
        val message:  TextView = itemView.findViewById(R.id.message)
        val lastModifiedData: TextView = itemView.findViewById(R.id.last_modified)

        val brand = ConversationRepo.getBrand(conversation.submissionId)
        brandName.text = brand.title

        parentActivity.activityModel.getMessage(conversation.id).observe(parentActivity, Observer {
            //TODO(teddy) the API should sort by date sent
            if(it.isNotEmpty())
                message.text = it[0].message
        })

        //Note(teddy) Simplify this, show dates for more than 24hrs and time for past 24hrs
        val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
        lastModifiedData.text = dateFormatter.format(conversation.lastModified)


        itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putInt(CONVERSATION_ID, conversation.id)
            }
            val intent  = Intent(parentActivity, ConversationActivity::class.java).apply {
                putExtras(bundle)
            }

            parentActivity.startActivity(intent)
        }
    }
}

class ConversationListAdapter(
    private val parentActivity: ConversationsList,
    private val conversations: List<Conversation>
) : RecyclerView.Adapter<ConversationViewHolder>() {

    override fun getItemCount(): Int = conversations.size

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(conversations[position], parentActivity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {

        val view = parentActivity.layoutInflater.inflate(
            R.layout.layout_conversation_card,
            parent,
            false
        )

        return ConversationViewHolder(view)
    }
}

class ConversationViewModel() : ViewModel() {

    val conversations: MutableLiveData<List<Conversation>> get() = ConversationRepo.conversations

    fun getMessage(convoId: ConversationID) : MutableLiveData<List<Message>> {
        return ConversationRepo.messages[convoId]
            ?: throw IllegalStateException("NOT SUPPOSED TO HAPPEN")
    }

    fun loadConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            ConversationRepo.loadConversations()
        }
    }

    fun loadMessages(convoId: Int) {
        viewModelScope.launch {
            ConversationRepo.loadMessages(convoId)
        }
    }
}