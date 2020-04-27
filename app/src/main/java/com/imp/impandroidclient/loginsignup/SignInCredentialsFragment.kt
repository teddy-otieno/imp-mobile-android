package com.imp.impandroidclient.loginsignup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.imp.impandroidclient.R
import kotlinx.android.synthetic.main.fragment_get_credentials.*

class SignInCredentialsFragment : Fragment() {


    private var listener: SignInCredentialsFragmentListener? = null;

    interface SignInCredentialsFragmentListener {
        fun collectCredentialsAndSignUp(creatorUsername: String, password: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_get_credentials, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is SignInCredentialsFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + "Must implemented SignInCredentialsFragmentListener")
        }

    }

    override fun onDetach() {
        super.onDetach()

        listener = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sign_up_confirm_password_field.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val match =
                    sign_up_password_field.text.toString() == sign_up_confirm_password_field.text.toString()

                println("Password validation")
                println(match)
                if (!match) {
                    sign_up_password_field.setBackgroundResource(R.drawable.input_field_error)
                    v.setBackgroundResource(R.drawable.input_field_error)
                } else {
                    v.setBackgroundResource(R.drawable.input_field)
                    sign_up_password_field.setBackgroundResource(R.drawable.input_field)
                }
            } else {
                v.setBackgroundResource(R.drawable.input_field)
            }
        }

        sign_up_password_field.setOnFocusChangeListener { v: View, hasFocus: Boolean ->
            if (hasFocus) {
                v.setBackgroundResource(R.drawable.input_field)
            } else {
                if (sign_up_password_field.text.isEmpty() or sign_up_password_field.text.isBlank()) {
                    v.setBackgroundResource(R.drawable.input_field_error)
                }
            }
        }

        sign_up_username_field.setOnFocusChangeListener { v: View, hasFocus: Boolean ->
            if (hasFocus) {
                v.setBackgroundResource(R.drawable.input_field)
            }
        }

        creator_sign_up_button.setOnClickListener {
            if (validateInput()) {
                println("Input Validated")
                listener?.collectCredentialsAndSignUp(
                    sign_up_username_field.text.toString(),
                    sign_up_password_field.text.toString()
                )
            }
        }

    }

    private fun validateInput(): Boolean {
        var result = true
        val editTextElements: Array<EditText> = arrayOf(
            sign_up_confirm_password_field,
            sign_up_password_field,
            sign_up_username_field
        )

        for (element in editTextElements) {
            if (element.text.isBlank() or element.text.isEmpty()) {
                result = false
                element.setBackgroundResource(R.drawable.input_field_error)
            }
        }

        if (!terms_and_condition_agreement.isChecked) {
            result = false
            terms_and_condition_agreement.setTextColor(resources.getColor(R.color.colorError))
        }

        val isPasswordEqual =
            sign_up_password_field.text.toString() == sign_up_confirm_password_field.text.toString()
        if (!isPasswordEqual) {
            result = false
        }

        return result

    }


}