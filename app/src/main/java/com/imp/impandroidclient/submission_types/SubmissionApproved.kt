package com.imp.impandroidclient.submission_types

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.imp.impandroidclient.CAMPAIGN_ID
import com.imp.impandroidclient.R
import com.imp.impandroidclient.SUBMISSION_ID
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.repos.PostSubmissionRepo
import com.imp.impandroidclient.app_state.repos.models.PostSubmission
import kotlinx.android.synthetic.main.activity_submission_approved.*
import kotlinx.coroutines.Dispatchers
import java.lang.IllegalStateException
import java.util.*
import kotlin.properties.Delegates

class SubmissionApproved : AppCompatActivity() {

    private var submissionId by Delegates.notNull<Int>()

    private val viewModel by viewModels<SubmissionApprovedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submission_approved)


        val bundle =  intent.extras
            ?: throw IllegalStateException("Activity expects a bundle")

        submissionId = run {
            val id = bundle.getInt(SUBMISSION_ID)

            if(id < 1)
                throw IllegalStateException("Activity expects a Submission Id")
            else
                id
        }

        submission_image.clipToOutline = true

        viewModel.loadSubmission(submissionId);

        button_post_to_insta.setOnClickListener {
            postSubmissionToInstagram()
        }

        lifecycleScope.launchWhenCreated {

            caption.text = viewModel.submission.postCaption

            viewModel.submission.image_url?.let { url ->

                ResourceManager.onLoadImage(url) { bitmap ->
                    submission_image.setImageBitmap(bitmap)
                }

            }

        }

    }

    override fun onStart() {
        super.onStart()

        val clipboard =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clip = ClipData.newPlainText("SUBMISSION", viewModel.submission.postCaption)
        clipboard.setPrimaryClip(clip)

        Timer().apply {
            schedule(object: TimerTask() {

                override fun run() {

                    Snackbar.make(content_view, R.string.copy_clipboard, Snackbar.LENGTH_LONG)
                        .show()

                }

            }, 500)
        }

    }

    private fun postSubmissionToInstagram() {
    }
}

class SubmissionApprovedViewModel : ViewModel() {

    private lateinit var mSubmission: PostSubmission

    val submission get() = mSubmission

    fun loadSubmission(id: Int) {
        mSubmission = PostSubmissionRepo.getSubmissionById(id)
    }
}