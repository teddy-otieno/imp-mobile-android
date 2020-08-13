package com.imp.impandroidclient.start_up.signup

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.imp.impandroidclient.R
import kotlinx.android.synthetic.main.fragment_basic_creator_info.*
import java.text.SimpleDateFormat
import java.util.*

class BasicSignUpFragment : Fragment() {

    val parentViewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =  inflater.inflate(R.layout.fragment_basic_creator_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setUpObservers()

        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.gender,
            R.layout.support_simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            gender_selector.adapter = adapter
        }
    }

    private fun validateInfo(): Boolean {
        var result = true
        val validateInput = { view: TextView ->
            if (view.text.isEmpty() or view.text.isBlank())
            {
                view.setBackgroundResource(R.drawable.input_field_error)
                result = false
            }
        }
        //Validate the first name and last_name
        validateInput(creator_first_name)
        validateInput(creators_last_name)

        return result
    }

    private fun setupListeners() {

        next_button.setOnClickListener {
            if (validateInfo())
            {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.sign_up_stage_fragment_container,
                        ContactInfoFragment()
                    )
                    .addToBackStack(null).commit()
            }
        }

        date_of_birth.setOnClickListener {
            activity?.run {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, set_year, set_month, dayOfMonth ->
                    val dateString = "$dayOfMonth-${(set_month + 1)}-$set_year"
                    date_of_birth.text = SpannableStringBuilder(dateString)
                    parentViewModel.dateOfBirth.value = SimpleDateFormat("dd-MM-yyyy").parse(dateString)
                }, year, month, day)
                datePickerDialog.show()
            } ?: throw IllegalStateException("Activity cannot be null")
        }

        creator_first_name.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus)
            {
                parentViewModel.fName.value = creator_first_name.text.toString()
            }
        }
        creators_last_name.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus)
            {
                parentViewModel.lName.value = creators_last_name.text.toString()
            }
        }
        gender_selector.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long ) {
                parent?.run {
                    parentViewModel.gender.value = getItemAtPosition(position) as String
                } ?: throw java.lang.IllegalStateException("Adapter view is not supposed to be null")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun setUpObservers() {
        activity?.run {
            parentViewModel.fName.observe(this, Observer {
                creator_first_name.text = SpannableStringBuilder(it)
            })
            parentViewModel.lName.observe(this, Observer {
                creators_last_name.text = SpannableStringBuilder(it)
            })
            parentViewModel.dateOfBirth.observe(this, Observer {
                val formatter = SimpleDateFormat("dd-MM-yyyy")
                date_of_birth.text = SpannableStringBuilder(formatter.format(it))
            })
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}