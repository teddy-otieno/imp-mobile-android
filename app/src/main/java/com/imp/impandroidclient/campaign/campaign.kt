package com.imp.impandroidclient.campaign

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.android.volley.Response
import com.imp.impandroidclient.GlobalApplication
import com.imp.impandroidclient.R
import com.imp.impandroidclient.helpers.CampaignParcelable
import com.imp.impandroidclient.helpers.ImageRequest
import kotlinx.android.synthetic.main.activity_campaign.*
import kotlin.math.pow

class Campaign : AppCompatActivity() {

    private var viewModel: CampaignViewModel? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign)

        val campaignObject: CampaignParcelable = intent.getParcelableExtra("CampaignObject")!!
        val viewModel: CampaignViewModel by viewModels()
        viewModel.setData(campaignObject)

        viewModel.campaignData.observe(this, Observer { campaign ->
            detailed_campaignCoverImage.setImageBitmap(GlobalApplication.memCache.get(campaign.cover_image))
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

        val imageSubmission: Button = submissionTypeChooser.findViewById(R.id.imageSubmissionButton)
        imageSubmission.setOnClickListener {
            TODO("Switch to image submission activity")
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


        //Fetch the brand avatar
        lifecycleScope.launchWhenCreated {
            val headers: Map<String, String> =
                mapOf("Authorization" to "Bearer " + GlobalApplication.accessToken)

            val request = ImageRequest(
                GlobalApplication.root_path + campaignObject.brand.brand_image,
                headers as MutableMap<String, String>,
                Response.Listener { image: Bitmap ->
                    brandOfCampaign.setImageBitmap(image)
                },
                Response.ErrorListener {
                    //TODO(" Handle volley error")
                }
            )

            GlobalApplication.httpRequestQueue.add(request)
        }
    }
}
