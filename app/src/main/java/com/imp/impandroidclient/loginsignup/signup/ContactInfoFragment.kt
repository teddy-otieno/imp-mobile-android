package com.imp.impandroidclient.loginsignup.signup

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.repos.SessionRepository
import com.imp.impandroidclient.dashboards.MainDashboard
import kotlinx.android.synthetic.main.fragment_contact_info.*

class ContactInfoFragment : Fragment() {

    private val parentViewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_contact_info, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpObservers()
        setUpListeners()
    }

    private fun setUpObservers() {
        activity?.run {
            parentViewModel.email.observe(this, Observer {
                creator_email.text = SpannableStringBuilder(it)
            })
            parentViewModel.phoneNumber.observe(this, Observer {
                creator_number.text = SpannableStringBuilder(it)
            })
            parentViewModel.username.observe(this, Observer {
                creator_username.text = SpannableStringBuilder(it)
            })
            parentViewModel.password.observe(this, Observer {
                creator_password.text = SpannableStringBuilder(it)
            })
            parentViewModel.agreement.observe(this, Observer {
                terms_conditions_radio_button.isChecked = it
            })

            SessionRepository.isAuthenticated.observe(this, Observer { authenticated ->

                if(authenticated) {
                    val intent = Intent(this, MainDashboard::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }

                    startActivity(intent)
                }

            })
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setUpListeners() {

        creator_email.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) {
                parentViewModel.email.value = creator_email.text.toString()
            }
        }
        creator_number.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) {
                parentViewModel.phoneNumber.value = creator_number.text.toString()
            }
        }
        creator_username.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) {
                parentViewModel.username.value = creator_username.text.toString()
            }
        }
        creator_password.setOnFocusChangeListener{ _, hasFocus ->
            if(!hasFocus) {
                parentViewModel.password.value = creator_password.text.toString()
            }
        }
        terms_conditions_radio_button.setOnCheckedChangeListener { buttonView, isChecked ->
            parentViewModel.agreement.value = isChecked
        }

        submit.setOnClickListener {

            //TODO(teddy) probably collect from other views too
            parentViewModel.password.value = creator_password.text.toString()

            if(validate()) {
                parentViewModel.submit()
            }
        }
    }

    private fun validate(): Boolean {
        var valid = true
        val checkEmpty = { textView: TextView ->

            if(textView.text.isEmpty() || textView.text.isBlank()) {
                textView.background = resources.getDrawable(R.drawable.input_field_error, null)
                valid = false
            }

        }

        checkEmpty(creator_email)
        checkEmpty(creator_number)
        checkEmpty(creator_username)
        checkEmpty(creator_password)

        if(!terms_conditions_radio_button.isChecked) {
            terms_conditions_radio_button.setTextColor(ResourcesCompat.getColor(resources, R.color.colorError, null))
            valid = false
        }

        return valid
    }
}
