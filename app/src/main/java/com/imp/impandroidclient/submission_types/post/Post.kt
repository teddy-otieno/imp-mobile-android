package com.imp.impandroidclient.submission_types.post

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.Cache
import com.imp.impandroidclient.media.MediaGallery
import com.imp.impandroidclient.submission_types.MediaChoiceBottomSheet
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.layout_submission_details.*

const val SELECT_IMAGE: Int = 0x1

//TODO(teddy) Need to fix the post activity layout
class Post : AppCompatActivity()
{
    private lateinit var viewModel: PostViewModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val submissionId = intent.getIntExtra("submissionID", -1)
        val campaignId = intent.getIntExtra("campaignId", -1)

        if(campaignId < 0) throw IllegalStateException("Campaign was not supplied in the intent")
        viewModel = PostViewModelFactory(submissionId, campaignId).create(PostViewModel::class.java)

        setUpListeners()
        setUpObservers()
    }

    private fun setUpListeners()
    {
        post_back_button.setOnClickListener { finish() }
        btn_submit_post.setOnClickListener {
            if(viewModel.submit())
            {
                viewModel.transferStatus().observe(this, Observer {
                    //Redirect to the homepage,
                    //Display a dialog successful
                    //Change the color of the submit button
                })
            }
            else
            {
                //TODO: Check for an error
            }
        }

        post_caption.setOnFocusChangeListener { v , hasFocus ->
            if(!hasFocus)
            {
                viewModel.submission.value?.run {
                    val copy = this.copy(postCaption=post_caption.text.toString())
                    viewModel.submission.value = copy
                }
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
            val postCallback = { ->
                val intent = Intent(this, MediaGallery::class.java)
                startActivityForResult(intent, SELECT_IMAGE)
            }

            MediaChoiceBottomSheet(this, postCallback).apply {
                show(supportFragmentManager, tag)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == SELECT_IMAGE)
        {
            if(resultCode == RESULT_OK)
            {
                data?.extras?.getParcelable<Uri>("IMAGE")?.run {
                    val image = Cache.getImageFromMemCache(this.toString())
                    viewModel.image.value = image
                } ?: throw IllegalStateException("POST activity: Image uri was not passed in the intent")
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

        viewModel.image.observe(this, Observer {
            post_image.imageTintMode = null
            post_image.setImageBitmap(it)
        })
    }
}
