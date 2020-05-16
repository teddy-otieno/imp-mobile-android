package com.imp.impandroidclient.campaign

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.Cache
import kotlinx.android.synthetic.main.activity_campaign.*
import kotlinx.android.synthetic.main.layout_campaign_info_details.*
import kotlinx.android.synthetic.main.layout_choose_submission.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CampaignActivity : AppCompatActivity() {

    private lateinit var campaignModelFactory: CampaignViewModelFactory
    private lateinit var viewModel: CampaignViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)

        initMembers()
        setUpObservers()
        setUpListeners()
        setupBottomSheet()

        lifecycleScope.launchWhenCreated {
            val campaign = viewModel.getCampaign().value!!
        }

    }

    private fun initMembers() {
        val campaignId: Int = intent.getIntExtra("campaignId", -1)

        if(campaignId == -1) {
            throw IndexOutOfBoundsException("CampaignID is out of range")
        }
        campaignModelFactory = CampaignViewModelFactory(campaignId)
        viewModel = ViewModelProvider(this, campaignModelFactory).get(CampaignViewModel::class.java)
    }

    private fun setUpListeners() {

        /*
        campaignSubmission.setOnClickListener {
            setupBottomSheet()
        }
         */

    }

    private fun setUpObservers() {

        viewModel.getCampaign().observe(this, Observer { campaign ->
            detailed_campaignCoverImage.setImageBitmap(Cache.getImageFromMemCache(campaign.cover_image))
            detail_campaignTitle.text = campaign.title
            detailed_campaignDescription.text = campaign.about_you
            detailed_contentWedLoveFromYou.text = campaign.content_wed_love_from_you
            detailed_whereToFindProduct.text = campaign.where_to_find_product


            val cachedBrandImage = Cache.getImageFromMemCache(campaign.brand.brand_image)
            if(cachedBrandImage == null) {
                lifecycleScope.launch(Dispatchers.Main) {
                    //TODO: Handle this instead of crashing the app
                    val brandImage = campaign.brand.brandImageFuture?.await()

                    if(brandImage == null) {
                        TODO("Handle this error")
                    } else {
                        brandOfCampaign.setImageBitmap(brandImage)
                        brandOfCampaign.clipToOutline = true
                    }
                }
            } else {
                brandOfCampaign.setImageBitmap(cachedBrandImage)
            }


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
    }

    private fun setupBottomSheet() {

        val sheetBehavior = BottomSheetBehavior.from(submissionTypeChooser)

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
