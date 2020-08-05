package com.imp.impandroidclient.submission_types

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.R
import com.imp.impandroidclient.SUBMISSION_ID
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.repos.PostSubmissionRepo
import com.imp.impandroidclient.app_state.repos.data.PostSubmission
import kotlinx.android.synthetic.main.activity_post_submission_view.*
import java.lang.IllegalStateException
import kotlin.properties.Delegates


/**
 * Expect SUBMISSION_ID
 */
class PostSubmissionView : AppCompatActivity() {

    private val model by viewModels<PostSubmissionViewModel>()
    private var submissionId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_submission_view)

        val bundle: Bundle = intent.extras ?: throw IllegalStateException("EXPECTED A BUNDLE")

        submissionId = bundle.getInt(SUBMISSION_ID)
        if(submissionId == 0) {
            throw IllegalStateException("SUBMISSION ID WAS NOT PROVIDED")
        }

        model.loadSubmissionId(submissionId)
        setUp()
    }

    private fun setUp() {
        model.submission.observe(this, Observer {
            caption.text = it.postCaption
            fee_rate.text = it.fee?.toString()
            notes.text = it.note

            it.image_url?.let { image_url ->
                ResourceManager.onLoadImage(image_url) { bitmap ->
                    submission_image.setImageBitmap(bitmap)
                }
            }
        })
    }
}

class PostSubmissionViewModel: ViewModel() {

    val submission: MutableLiveData<PostSubmission> = MutableLiveData()

    fun loadSubmissionId(id: Int) {
        submission.value = PostSubmissionRepo.getSubmissionById(id)
    }
}