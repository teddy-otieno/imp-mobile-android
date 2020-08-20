package com.imp.impandroidclient.main_screen.ui.creator_profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.appbar.MaterialToolbar
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.repos.UserAccount
import com.imp.impandroidclient.app_state.repos.data.CreatorAccount
import com.imp.impandroidclient.main_screen.DashBoardFragment
import com.imp.impandroidclient.messaging.ConversationsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreatorProfile : DashBoardFragment() {

    private val viewModel: CreatorProfileViewModel by activityViewModels()

    private lateinit var creatorAvatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadUserProfile()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_creator_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        creatorAvatar = view.findViewById(R.id.creator_avatar)
        creatorAvatar.clipToOutline = true
    }

    override fun updateToolBar(view: MaterialToolbar) {
        view.title = resources.getString(R.string.profile)
        view.inflateMenu(R.menu.creator_frag_menu)

        view.setOnMenuItemClickListener {

            when(it.itemId) {

                R.id.message_men_icon -> {
                    val intent = Intent(activity, ConversationsList::class.java)
                    startActivity(intent)

                    true
                }

                else -> false
            }
        }
    }
}

class CreatorProfileViewModel : ViewModel() {

    fun getUserAccount(): MutableLiveData<CreatorAccount> = UserAccount.account

    fun loadUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            UserAccount.loadUser()
        }
    }
}
