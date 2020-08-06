package com.imp.impandroidclient.submission_types

import android.content.ContentResolver
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
import com.imp.impandroidclient.*
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.repos.CampaignRepository
import com.imp.impandroidclient.app_state.repos.PostSubmissionRepo
import com.imp.impandroidclient.app_state.repos.data.CampaignData
import com.imp.impandroidclient.app_state.repos.data.HashTag
import com.imp.impandroidclient.app_state.repos.data.PostSubmission
import kotlinx.android.synthetic.main.activity_post_submission_details.*
import kotlin.NumberFormatException
import kotlin.properties.Delegates


/**
 *
 * When Edit mode pass the submissionID
 */
class PostSubmissionDetails : AppCompatActivity() {

    private val model: PostSubmissionDetailsViewModel by viewModels()

    private var imageUri: Uri? = null
    private var campaignId by Delegates.notNull<Int>()
    private var editMode by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_submission_details)

        val bundle = intent.extras ?: throw IllegalStateException("EXPECTED A BUNDLE")
        editMode = bundle.getInt(EDIT_MODE)
        campaignId = bundle.getInt(CAMPAIGN_ID)


        imageUri = bundle.getParcelable(IMAGE_URI)

        CampaignRepository.loadHashTags(campaignId)

        if(editMode == EDIT_SUBMISSION_RESULT) {
            val submissionId = bundle.getInt(SUBMISSION_ID)
            if(submissionId == 0) {
                throw IllegalStateException("SUBMISSION WAS EXPECTED WHEN EDITING")
            }

            model.loadSubmission(submissionId)
        } else {
            if(campaignId == 0) { throw IllegalStateException("EXPECTED A CAMPAIGN ID") }
            if(imageUri == null) throw IllegalStateException("EXPECTED IMAGE URI")
        }

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

        imageUri?.let {
            val submissionBitmap = ResourceManager.getThumbnailBitmap(it)
                ?: throw IllegalStateException("EXPECTED AN BITMAP")
            submission_thumbnail.setImageBitmap(submissionBitmap)
        }

        model.captionData.observe(this, Observer {

            if(caption.text.toString() != it) {
                caption.setText(it)
            }
        })

        model.feeData.observe(this, Observer {

            try {

                if(Integer.parseInt(fee_rate.text.toString()) != it) {
                    fee_rate.setText(it.toString())
                }
            } catch(e: NumberFormatException) {
                Log.i("EXCEPTION", e.message ?: "Fee field was empty")
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

                    if(editMode == EDIT_SUBMISSION_RESULT) {
                        model.submitEdit()
                    } else {
                        imageUri?.let { it1 -> model.submit(campaignId, it1, contentResolver) }

                    }
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

    private lateinit var submissionTobeEdited: PostSubmission
    private var subId: Int by Delegates.notNull()

    fun getHashTags(): MutableLiveData<List<HashTag>> = CampaignRepository.hashTags

    fun submit(campaign: Int, image: Uri, contentResolver: ContentResolver) {
        PostSubmissionRepo.syncSubmission(
            PostSubmission(
                campaignId = campaign,
                postCaption = captionData.value,
                fee = feeData.value,
                note = noteData.value
            ),
            image,
            contentResolver
        )
    }

    fun loadSubmission(submissionId: Int) {
        subId = submissionId

        submissionTobeEdited = PostSubmissionRepo.getSubmissionById(submissionId)

        submissionTobeEdited.apply {
            captionData.value = postCaption
            feeData.value = fee
            noteData.value = note
        }

    }

    fun submitEdit() {

        submissionTobeEdited.apply {
            postCaption = captionData.value
            fee = feeData.value
            note = noteData.value
        }

        PostSubmissionRepo.patchSubmission(submissionTobeEdited)
    }
}