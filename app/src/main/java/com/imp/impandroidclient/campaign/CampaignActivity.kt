package com.imp.impandroidclient.campaign

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.Cache
import com.imp.impandroidclient.image_submission.ImageSubmission
import kotlinx.android.synthetic.main.activity_campaign.*
import kotlin.math.pow

class CampaignActivity : AppCompatActivity() {

    private lateinit var campaignModelFactory: CampaignViewModelFactory
    private lateinit var viewModel: CampaignViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)

        val campaignId: Int = intent.getIntExtra("campaignId", -1)

        if(campaignId == -1) {
            throw IndexOutOfBoundsException("CampaignID is out of range")
        }
        campaignModelFactory = CampaignViewModelFactory(campaignId)

        viewModel = ViewModelProvider(this, campaignModelFactory).get(CampaignViewModel::class.java)

        viewModel.getCampaign().observe(this, Observer { campaign ->
            detailed_campaignCoverImage.setImageBitmap(Cache.getImageFromMemCache(campaign.cover_image))
            detail_campaignTitle.text = campaign.title
            detailed_campaignDescription.text = campaign.about_you
            detailed_contentWedLoveFromYou.text = campaign.content_wed_love_from_you
            detailed_whereToFindProduct.text = campaign.where_to_find_product

            for (item in campaign.dos) {
                val view: ConstraintLayout =
                    layoutInflater.inflate(R.layout.layout_list_row, null) as ConstraintLayout
                val textView = view.getViewById(R.id.row_text) as TextView
                textView.text = item.name

                campaignDos.addView(view)
            }

            for (item in campaign.dos) {
                val view: ConstraintLayout =
                    layoutInflater.inflate(R.layout.layout_list_row, null) as ConstraintLayout
                val textView = view.getViewById(R.id.row_text) as TextView
                textView.text = item.name

                campaignDonts.addView(view)
            }

        })

        submissionTypeChooser.visibility = View.GONE
        setUpListeners()

        lifecycleScope.launchWhenCreated {

            val campaign = viewModel.getCampaign().value!!


        }
    }

    private fun setUpListeners() {
        val imageSubmission: Button = submissionTypeChooser.findViewById(R.id.imageSubmissionButton)
        imageSubmission.setOnClickListener {
            //TODO("Switch to image submission activity")

            val intent = Intent(this, ImageSubmission::class.java)
            intent.putExtra("campaignId", viewModel.campaignId)

            this.startActivity(intent)
        }

        val videoSubmission: Button = submissionTypeChooser.findViewById(R.id.videoSubmissionButton)
        videoSubmission.setOnClickListener {
            TODO("Switch to video capture submission")
        }

        campaignSubmission.setOnClickListener {
            submissionTypeChooser.visibility = View.VISIBLE

            val animatorSet: AnimatorSet = AnimatorSet()

            val animation: ObjectAnimator =
                ObjectAnimator.ofFloat(submissionTypeChooser, "translationY", 200.0f, 0.0f)
            animation.duration = 500
            animation.interpolator = TimeInterpolator { x ->
                (1 - (1.0 - x).pow(2.0)).toFloat()
            }

            animatorSet.play(animation)
            animatorSet.start()

        }
    }
}
