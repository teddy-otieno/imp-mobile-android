package com.imp.impandroidclient.main_screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.ActivityNavigator
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.repos.ConversationRepo
import com.imp.impandroidclient.app_state.repos.models.Conversation
import com.imp.impandroidclient.service.MSG_LISTEN_CONVERSATION
import com.imp.impandroidclient.service.NetworkService
import kotlinx.android.synthetic.main.activity_main_dashboard.*
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

class MainDashboard : AppCompatActivity() {

    private val mViewModel by viewModels<MainDashboardViewModel>()

    private var mService: Messenger? = null
    private var mBound = false

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = Messenger(service)
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_dashboard)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)

        navView.setupWithNavController(navController)

        ActivityNavigator.applyPopAnimationsToPendingTransition(this)

        tool_bar.setNavigationOnClickListener {
            drawer_layout.openDrawer(GravityCompat.START)
        }

        mViewModel.getConversations().observe(this, Observer { conversations ->

            for(conversation in conversations) {
                val message = android.os.Message.obtain(
                    null,
                    MSG_LISTEN_CONVERSATION,
                    conversation.id,
                    0
                )
                mService?.send(message)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        Intent(this, NetworkService::class.java).also {intent ->
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }
}

abstract class DashBoardFragment: Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: MaterialToolbar = activity?.findViewById(R.id.tool_bar)
            ?: throw IllegalStateException()

        toolbar.menu.clear()
        updateToolBar(toolbar)
    }

    abstract fun updateToolBar(view: MaterialToolbar);

}

class MainDashboardViewModel : ViewModel() {

    fun getConversations(): MutableLiveData<List<Conversation>> {
        viewModelScope.launch {
            ConversationRepo.loadConversations()
        }

        return ConversationRepo.conversations
    }
}