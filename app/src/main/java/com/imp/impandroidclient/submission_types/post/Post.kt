package com.imp.impandroidclient.submission_types.post

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.imp.impandroidclient.R
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.layout_submission_details.*

class Post : AppCompatActivity()
{
    private lateinit var viewModel: PostViewModel
    private lateinit var viewModelFactory: PostViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val submissionId = intent.getIntExtra("submissionID", -1)
        val campaignId = intent.getIntExtra("campaignId", -1)

        if(campaignId < 0) throw RuntimeException("Undefined Behaviour, campaign was not supplied in the intent")
        viewModel = PostViewModelFactory(submissionId, campaignId).create(PostViewModel::class.java)

        setUpListeners()
        setUpObservers()
    }

    private fun setUpListeners()
    {
        post_back_button.setOnClickListener { finish() }
        btn_submit_post.setOnClickListener {
            viewModel.submit()

            viewModel.transferStatus().observe(this, Observer {

            } )
        }

        post_caption.setOnFocusChangeListener { v , hasFocus ->
            if(!hasFocus)
            {
                val newSubmission = viewModel.submission.value!!
                newSubmission.postCaption = post_caption.text.toString()
                viewModel.submission.value = newSubmission
            }
        }

        post_fee_rates.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus)
            {
                val newSubmission = viewModel.submission.value!!
                Log.d("PARSE", "|${(v as TextView).text}|")
                newSubmission.fee = post_fee_rates.text.toString().toInt()
                viewModel.submission.value = newSubmission
            }
        }

        post_notes.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus)
            {
                val newSubmission = viewModel.submission.value!!
                newSubmission.note = post_notes.text.toString()
                viewModel.submission.value = newSubmission
            }
        }

        media_display.setOnClickListener {
            val bottomSheetDialog = PostMediaBottomSheet().apply {
                show(supportFragmentManager, tag)
            }
        }
    }

    private fun setUpObservers()
    {
        viewModel.submission.observe(this, Observer {
            post_caption.setText(it.postCaption)
            it.fee?.let { fee ->
                post_fee_rates.text = SpannableStringBuilder(fee.toString())
            }
            post_notes.setText(it.note)
        })
    }
}
