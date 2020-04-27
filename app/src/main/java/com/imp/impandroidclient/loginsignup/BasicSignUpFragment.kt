package com.imp.impandroidclient.loginsignup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.imp.impandroidclient.R
import kotlinx.android.synthetic.main.basic_creator_info_fragment.*
import java.util.*

class BasicSignUpFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private lateinit var dateSelected: Date

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(
            R.layout.basic_creator_info_fragment,
            container, false
        )

        return view
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.gender,
            R.layout.support_simple_spinner_dropdown_item
        )
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
                gender_selector.adapter = adapter
            }

        next_button.setOnClickListener {
            if (validateInfo()) {
                //Proceed to the next fragment once all information is entered
                listener?.getBasicInfo(
                    creator__first_name.text.toString(),
                    creators_last_name.text.toString(),
                    gender_selector.selectedItem.toString()
                )
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.sign_up_stage_fragment_container, DateOfBirthFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }


        //Set listener for edit text fields
        val editTextsViews = arrayOf<EditText>(
            creator__first_name,
            creators_last_name
        )

        for (editView in editTextsViews) {
            editView.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    //check if the
                    if (editView.resources.getIdentifier("input_field", "drawable", null) == 0) {
                        editView.setBackgroundResource(R.drawable.input_field)
                    }
                }
            }
        }
    }

    private fun validateInfo(): Boolean {
        var result = true

        val validateInput = { view: TextView ->
            if (view.text.isEmpty() or view.text.isBlank()) {
                view.setBackgroundResource(R.drawable.input_field_error)
                result = false
            }
        }
        //Validate the first name and last_name
        validateInput(creator__first_name)
        validateInput(creators_last_name)

        return result
    }

    interface OnFragmentInteractionListener {
        fun getBasicInfo(firstName: String, lastName: String, gender: String)
    }
}