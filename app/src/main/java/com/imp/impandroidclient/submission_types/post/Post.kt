package com.imp.impandroidclient.submission_types.post

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.imp.impandroidclient.CAMPAIGN_ID
import com.imp.impandroidclient.R
import com.imp.impandroidclient.SUBMISSION_ID
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.repos.TransferStatus
import com.imp.impandroidclient.media.MediaGallery
import com.imp.impandroidclient.submission_types.MediaChoiceBottomSheet
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.layout_submission_details.*
import kotlinx.coroutines.launch

//Note(teddy) This is a flag for result expected by this activity
const val SELECT_IMAGE: Int = 0x1


/**
 * TODO(teddy) Need to fix the post activity layout
 */
class Post : AppCompatActivity()
{
    private lateinit var viewModel: PostViewModel
    private lateinit var buttonText: String
    private val shortAnimationDuration: Int = 1


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)


        val submissionId = intent.getIntExtra(SUBMISSION_ID, -1)
        val campaignId = intent.getIntExtra(CAMPAIGN_ID, -1)

        if(campaignId < 0) throw IllegalStateException("Campaign was not supplied in the intent")
        viewModel = PostViewModelFactory(submissionId, campaignId).create(PostViewModel::class.java)

        /*
           FIXME(teddy) Proportion problem on different aspect ratios
         */
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        linearLayout.layoutParams = LinearLayout.LayoutParams(metrics.widthPixels, metrics.widthPixels)
        if(viewModel.isExisting)
        {
            /*
                Note(teddy) Change the button string to update, when we are editing a submission
             */
            val text = resources.getString(R.string.update)
            btn_submit_post.text = text
            buttonText = text
        }
        else
        {
            buttonText = resources.getString(R.string.submit)
        }
        setUpListeners()
        setUpObservers()
    }

    private fun getColorVal(id: Int): Int
    {
        return if(Build.VERSION.SDK_INT >= 23)
            resources.getColor(R.color.transparent, null)
        else
            resources.getColor(R.color.transparent)
    }

    private fun setUpListeners()
    {
        post_back_button.setOnClickListener { finish() }
        btn_submit_post.setOnClickListener {
            post_notes.clearFocus()
            post_caption.clearFocus()
            post_fee_rates.clearFocus()

            if(viewModel.submit())
            {
                viewModel.transferStatus().observe(this, Observer {status ->
                    //Redirect to the homepage,
                    //Display a dialog successful
                    //Change the color of the submit button
                    //TODO("Provide feedback to the user during the transfer process")

                    /*
                    val transparent = getColorVal(R.color.transparent)

                    val textColorToTransparent = ObjectAnimator.ofArgb(btn_submit_post, "textColor", transparent).apply {
                        duration = 1000
                        addListener(object: AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                btn_submit_post.text = resources.getString(R.string.done)
                            }
                        })
                    }

                    val greenCol = getColorVal(R.color.white)
                    val textColorToNormal = ObjectAnimator.ofArgb(btn_submit_post, "textColor", transparent, greenCol).apply {
                        duration = 1000
                    }
                     */

                    /**
                     * Going to animate the text buttons
                     */
                    when(status)
                    {
                        TransferStatus.INPROGRESS -> {
                            btn_submit_post.text = resources.getString(R.string.uploading)
                        }
                        TransferStatus.SUCESSFULL -> {
                            /*
                            val backgroundColor = ObjectAnimator.ofArgb(
                                btn_submit_post,
                                "background",
                                getColorVal(R.color.colorPrimary),
                                getColorVal(R.color.green)

                            ).apply { duration = 1000 }

                            AnimatorSet().apply {
                                play(textColorToNormal).with(backgroundColor)
                                start()
                            }
                             */

                            btn_submit_post.text = resources.getString(R.string.done)
                        }

                        TransferStatus.FAILED -> {
                            btn_submit_post.text = resources.getString(R.string.failed)
                        }

                        TransferStatus.NOTHING -> {
                            btn_submit_post.text = buttonText
                        }
                    }
                })
            }
            else
            {
                TODO("Validation failed")
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

    /**
     * TODO(teddy) cross check if the image is similiar to the previous \
     * before setting the imageChanged flag
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == SELECT_IMAGE)
        {
            if(resultCode == RESULT_OK)
            {
                data?.extras?.getParcelable<Uri>("IMAGE")?.let {uri ->
                    /**
                     * Note(teddy) Optimization : Maybe store all images in the cache \
                     * instead of the object
                     */
                    viewModel.submission.value?.let {
                        val tempSubmission = it.copy(image_url=uri.toString())

                        if(viewModel.isExisting)
                            viewModel.imageChanged = true

                        viewModel.submission.value = tempSubmission
                    } ?: throw java.lang.IllegalStateException("Post Submission is not supposed to be null")
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

            post_image.imageTintMode = null

            //TODO(teddy) Problem will arise during patching

            it.image_url?.let { url ->
                ResourceManager.getImage(url)?.let { bitmap ->
                    post_image.setImageBitmap(bitmap)
                }
            }
        })
    }
}
