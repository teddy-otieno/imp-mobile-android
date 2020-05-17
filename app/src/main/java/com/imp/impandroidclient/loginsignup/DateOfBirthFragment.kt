package com.imp.impandroidclient.loginsignup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.imp.impandroidclient.R
import kotlinx.android.synthetic.main.fragment_date_of_birth.*
import java.util.*

class DateOfBirthFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    var day_validated: Boolean = false
    var year_validate: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_date_of_birth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        date_of_birth_day.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                day_validated = validateDay(
                    date_of_birth_month.selectedItemPosition + 1,
                    date_of_birth_day.text.toString().toInt()
                )

                if (!day_validated) {
                    date_of_birth_day.setBackgroundResource(R.drawable.input_field_error)
                }
            } else {
                date_of_birth_day.setBackgroundResource(R.drawable.input_field)
            }
        }

        date_of_birth_year.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val year = date_of_birth_year.text.toString().toInt()
                year_validate = ((year < 2000) and (year > 1940))
                if (!year_validate) {
                    date_of_birth_year.setBackgroundResource(R.drawable.input_field_error)
                }
            } else {
                date_of_birth_year.setBackgroundResource(R.drawable.input_field)
            }
        }

        date_of_birth_next.setOnClickListener {
            nextButtonClick(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun nextButtonClick(view: View) {
        date_of_birth_year.clearFocus()
        date_of_birth_day.clearFocus()

        val month = date_of_birth_month.selectedItemPosition + 1
        val day = date_of_birth_day.text.toString()
        val year = date_of_birth_year.text.toString()

        if (day_validated and year_validate) {
            listener?.onDateOfBirthInput(day, month, year)
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.sign_up_stage_fragment_container, ContactInfoFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun validateDay(month: Int, day: Int): Boolean {
        val listOfMonths31Days: ArrayList<Int> = arrayListOf(1, 3, 5, 7, 8, 10, 12)
        val listOfMonths30Days: ArrayList<Int> = arrayListOf(4, 6, 9, 11)

        if (listOfMonths31Days.contains(month)) {
            return ((day >= 1) and (day <= 31))
        } else if (listOfMonths30Days.contains(month)) {
            return ((day >= 1) and (day <= 30))
        } else if (month == 2) {
            return ((day >= 1) and ((day <= 28) or (day <= 29)))
        }

        return false
    }

    interface OnFragmentInteractionListener {
        fun onDateOfBirthInput(day: String, month: Int, year: String)
    }

}
