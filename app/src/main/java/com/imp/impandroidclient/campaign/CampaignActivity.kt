package com.imp.impandroidclient.campaign

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ActivityNavigator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.imp.impandroidclient.CAMPAIGN_ID
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.repos.data.GenericCampaignItem
import com.imp.impandroidclient.submission_types.ChooseMedia
import kotlinx.android.synthetic.main.activity_campaign.*
import kotlinx.android.synthetic.main.layout_campaign_info_details.*
import java.lang.IllegalStateException
import kotlin.properties.Delegates

/**
 * Expecting [CAMPAIGN_ID] passed in the bundle
 * throws IllegalStateException if absent
 */
class CampaignActivity : AppCompatActivity() {
    private var campaignId: Int by Delegates.notNull()

    private lateinit var campaignModelFactory: CampaignViewModelFactory
    private lateinit var viewModel: CampaignViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)

        ActivityNavigator.applyPopAnimationsToPendingTransition(this)

        initMembers()
        setUpObservers()
        setUpListeners()
    }

    private fun initMembers() {

        val bundle: Bundle = intent.extras ?: throw IllegalStateException("EXPECTED A BUNDLE")
        val campaignId: Int = bundle.getInt(CAMPAIGN_ID)

        if(campaignId == 0) { throw IllegalStateException("EXPECTED CAMPAIGN ID") }
        this.campaignId = campaignId

        campaignModelFactory = CampaignViewModelFactory(campaignId)
        viewModel = ViewModelProvider(this, campaignModelFactory)
            .get(CampaignViewModel::class.java)

        viewModel.loadDosAndDonts()
    }

    private fun setUpListeners() {
        campaign_tool_bar.setNavigationOnClickListener { finishAfterTransition() }

        campaign_fab.setOnClickListener {
            val bundle: Bundle = Bundle().apply {
                putInt(CAMPAIGN_ID, campaignId)
            }
            val intent = Intent(this, ChooseMedia::class.java).apply {
                putExtras(bundle)
            }

            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
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

        })

        val generateList = { items: List<GenericCampaignItem>, parentView: LinearLayout ->

            for((i, item) in items.withIndex()) {
                val view = layoutInflater.inflate(
                    R.layout.item_text_view,
                    null,
                    false) as TextView

                //FIXME(teddy) Bug with the index
                view.text = "${i + 1}. ${item.text}"

                parentView.addView(view)
            }
        }

        viewModel.dos.observe(this, Observer {
            generateList(it, campaign_dos)
        })

        viewModel.donts.observe(this, Observer {
            generateList(it, campaign_donts)
        })
    }

    override fun onBackPressed() {
        information_view.visibility = View.INVISIBLE
        finishAfterTransition()
    }
}
