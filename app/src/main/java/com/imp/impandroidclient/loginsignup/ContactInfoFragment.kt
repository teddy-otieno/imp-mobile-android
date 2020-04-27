package com.imp.impandroidclient.loginsignup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.imp.impandroidclient.R
import kotlinx.android.synthetic.main.fragment_contact_info.*

class ContactInfoFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        contact_details_next.setOnClickListener {
            listener?.onContactInformationRetrieved(
                contact_email_address.text.toString(),
                contact_phone_number.text.toString()
            )

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.sign_up_stage_fragment_container, SignInCredentialsFragment())
                .commit()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onContactInformationRetrieved(creatorEmail: String, creatorPhoneNumber: String)
    }
}
