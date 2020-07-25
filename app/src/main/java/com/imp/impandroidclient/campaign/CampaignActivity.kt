package com.imp.impandroidclient.campaign

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.imp.impandroidclient.CAMPAIGN_ID
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.submission_types.post.Post
import kotlinx.android.synthetic.main.activity_campaign.*
import kotlinx.android.synthetic.main.layout_campaign_info_details.*
import kotlinx.android.synthetic.main.layout_choose_submission.*

class CampaignActivity : AppCompatActivity() {

    private lateinit var campaignModelFactory: CampaignViewModelFactory
    private lateinit var viewModel: CampaignViewModel
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)

        initMembers()
        setUpObservers()
        setupBottomSheet()
        setUpListeners()
    }

    private fun initMembers() {
        val campaignId: Int = intent.getIntExtra("campaignId", -1)

        if(campaignId == -1) throw IndexOutOfBoundsException("CampaignID is out of range")
        campaignModelFactory = CampaignViewModelFactory(campaignId)
        viewModel = ViewModelProvider(this, campaignModelFactory)
            .get(CampaignViewModel::class.java)
    }

    private fun setUpListeners() {

        viewModel.campaignData.value?.let { campaignData ->
            post_submission.setOnClickListener {
                val intent = Intent(this, Post::class.java).apply {
                    this.putExtra(CAMPAIGN_ID, campaignData.id)
                }
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                startActivity(intent)
            }
        } ?: throw IllegalStateException("Attempting to create a campaign that has not been loaded")
    }

    private fun setUpObservers() {

        viewModel.campaignData.observe(this, Observer { campaign ->

            ResourceManager.onLoadImage(campaign.cover_image) {
                detailed_campaignCoverImage.setImageBitmap(it)
            }

            detail_campaignTitle.text = campaign.title
            detailed_about_us.text = campaign.about_you
            detailed_contentWedLoveFromYou.text = campaign.content_wed_love_from_you
            detailed_whereToFindProduct.text = campaign.where_to_find_product
            detailed_callToAction.text = campaign.call_to_action

            //brand_avatar.setImageBitmap(Cache.getImageFromMemCache(campaign.brand.brand_image))
            brand_avatar.clipToOutline = true

            /*
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
            */

        })
    }

    private fun setupBottomSheet() {

        sheetBehavior = BottomSheetBehavior.from(submissionTypeChooser)

        sheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        submissionTypeChooser.setOnClickListener {

            if(sheetBehavior.state  != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            } else {
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            }
        }
    }
}
