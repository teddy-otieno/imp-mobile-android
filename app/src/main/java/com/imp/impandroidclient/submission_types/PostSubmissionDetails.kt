package com.imp.impandroidclient.submission_types

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.android.material.chip.Chip
import com.imp.impandroidclient.CAMPAIGN_ID
import com.imp.impandroidclient.IMAGE_URI
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.repos.CampaignRepository
import com.imp.impandroidclient.app_state.repos.PostSubmissionRepo
import com.imp.impandroidclient.app_state.repos.data.CampaignData
import com.imp.impandroidclient.app_state.repos.data.HashTag
import com.imp.impandroidclient.app_state.repos.data.PostSubmission
import kotlinx.android.synthetic.main.activity_post_submission_details.*
import java.lang.NumberFormatException
import kotlin.properties.Delegates


class PostSubmissionDetails : AppCompatActivity() {

    private val model: PostSubmissionDetailsViewModel by viewModels()

    private lateinit var imageUri: Uri
    private var campaignId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_submission_details)

        val bundle = intent.extras ?: throw IllegalStateException("EXPECTED A BUNDLE")

        campaignId = bundle.getInt(CAMPAIGN_ID)
        if(campaignId == 0) { throw IllegalStateException("EXPECTED A CAMPAIGN ID") }

        imageUri = bundle.getParcelable(IMAGE_URI) ?: throw IllegalStateException("EXPECTED IMAGE URI")

        CampaignRepository.loadHashTags(campaignId)

        setUp()
    }

    private fun setUp() {
        //Load hashtags
        model.getHashTags().observe(this, Observer {

            it?.let { hashTags ->

                for(hashTag in hashTags) {
                    val chipView = Chip(hashtag_chipgroups.context).apply {
                        isClickable = true
                        isCheckable = true
                        text = "#${hashTag.hashtag}"
                    }
                    chipView.setOnClickListener {
                        hashtag_chipgroups.removeView(chipView)
                    }

                    hashtag_chipgroups.addView(chipView)
                }
            }

        })

        val submissionBitmap = ResourceManager.getThumbnailBitmap(imageUri)
            ?: throw IllegalStateException("EXPECTED AN BITMAP")
        submission_thumbnail.setImageBitmap(submissionBitmap)

        model.captionData.observe(this, Observer {

            if(caption.text.toString() != it) {
                caption.setText(it)
            }
        })

        model.feeData.observe(this, Observer {

            if(Integer.parseInt(fee_rate.text.toString()) != it) {
                fee_rate.setText(it.toString())
            }
        })

        model.noteData.observe(this, Observer {

            if(notes.text.toString() != it) {
                notes.setText(it)
            }
        })

        activity_tool_bar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.submit -> {
                    model.submit(campaignId, imageUri)
                    true
                }
                else -> throw IllegalStateException("UNKNOWN ITEM")
            }
        }

        setUpListeners()
    }

    private fun setUpListeners() {
        caption.doOnTextChanged { text, start, count, after ->
            model.captionData.value = text.toString()
        }

        fee_rate.doOnTextChanged { text, start, count, after ->

            try {
                model.feeData.value = Integer.parseInt(text.toString())
            } catch (e: NumberFormatException) {
                Log.w("CONVERSATION", e.message ?: "String is not a number")
                e.printStackTrace()
            }
        }

        notes.doOnTextChanged { text, start, count, after ->

            model.noteData.value = text.toString()

        }
    }
}

class PostSubmissionDetailsViewModel : ViewModel() {

    val captionData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val feeData: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val noteData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getHashTags(): MutableLiveData<List<HashTag>> = CampaignRepository.hashTags

    fun submit(campaign: Int, image: Uri) {
        PostSubmissionRepo.syncSubmission(
            PostSubmission(
                campaignId = campaign,
                postCaption = captionData.value,
                fee = feeData.value,
                note = noteData.value),
            image
        )
    }
}