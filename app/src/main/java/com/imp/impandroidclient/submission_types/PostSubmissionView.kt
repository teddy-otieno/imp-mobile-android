package com.imp.impandroidclient.submission_types

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.imp.impandroidclient.EDIT_MODE
import com.imp.impandroidclient.R
import com.imp.impandroidclient.SUBMISSION_ID
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.repos.PostSubmissionRepo
import com.imp.impandroidclient.app_state.repos.models.PostSubmission
import kotlinx.android.synthetic.main.activity_post_submission_view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import kotlin.properties.Delegates


const val EDIT_SUBMISSION_RESULT    = 0x001
const val PATCH_NEW_IMAGE           = 0x002

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
                lifecycleScope.launch(Dispatchers.Main) {
                    ResourceManager.onLoadImage(image_url) { bitmap ->
                        submission_image.setImageBitmap(bitmap)
                    }
                }
            }
        })

        edit_fab.setOnClickListener {

            val bundle = Bundle().apply {
                putInt(SUBMISSION_ID, submissionId)
                putInt(EDIT_MODE, EDIT_SUBMISSION_RESULT)
            }
            val intent = Intent(this, PostSubmissionDetails::class.java).apply {
                putExtras(bundle)
            }

            startActivityForResult(intent, EDIT_SUBMISSION_RESULT)
        }

        submission_image.setOnClickListener {
            val bundle = Bundle().apply {
                putInt(SUBMISSION_ID, submissionId)
                putInt(EDIT_MODE, PATCH_NEW_IMAGE)
            }
            val intent = Intent(this, ChooseMedia::class.java).apply {
                putExtras(bundle)
            }

            startActivityForResult(intent, PATCH_NEW_IMAGE)
        }

        app_toolbar.setNavigationOnClickListener {
            finishAfterTransition()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == EDIT_SUBMISSION_RESULT) {
            if(resultCode == Activity.RESULT_OK) {
                Log.i("RESULT", "Submission was edited")
            }
        }
    }
}

class PostSubmissionViewModel: ViewModel() {

    val submission: MutableLiveData<PostSubmission> = MutableLiveData()

    fun loadSubmissionId(id: Int) {
        submission.value = PostSubmissionRepo.getSubmissionById(id)
    }
}