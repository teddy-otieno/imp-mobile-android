package com.imp.impandroidclient.dashboards.ui.creator_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.imp.impandroidclient.R

class CreatorProfile : Fragment() {

    companion object {
        fun newInstance() = CreatorProfile()
    }

    private lateinit var viewModel: CreatorProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.creator_profile_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CreatorProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
