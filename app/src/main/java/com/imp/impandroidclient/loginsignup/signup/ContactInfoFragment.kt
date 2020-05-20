package com.imp.impandroidclient.loginsignup.signup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.imp.impandroidclient.R
import kotlinx.android.synthetic.main.fragment_contact_info.*

class ContactInfoFragment : Fragment() {

    private val parentViewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_contact_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        contact_details_next.setOnClickListener {
        }
    }
}
