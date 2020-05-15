package com.imp.impandroidclient.image_submission

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.Cache

typealias ImageID = Int

class ImageSubmission : AppCompatActivity() {

    private val viewModel : ImageSubmissionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_image_submission)


        //get submission id from intent
        //Incase our user wants to open already made campaign
        val submissionId = intent.getIntExtra("SubmissionId", -1)
        viewModel.loadSubmission(submissionId)

        viewModel.getTempSubmissionData().observe(this, Observer{ _sub ->

        })

        val campaignObject: Int = intent.getIntExtra("CampaignObject", -1)
        val layoutManger = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false)

        /*
        val images: List<ImageID> = listOf(campaignObject.cover_image, campaignObject.cover_image, campaignObject.cover_image)
        val adapter = SubmissionImagesCarouselAdapter(this, images)

        image_submission_carousel.adapter = adapter
        image_submission_carousel.layoutManager = layoutManger

         */

    }
}


class ImageViewHolder(val image: ConstraintLayout) : RecyclerView.ViewHolder(image)

class SubmissionImagesCarouselAdapter(val activityContext: Activity, private val images: List<String?>) :
    RecyclerView.Adapter<ImageViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {

        val submissionView = LayoutInflater.from(parent.context)
            .inflate(R.layout.frame_submission_image, parent, false) as ConstraintLayout

        //Set layout parameters

        return ImageViewHolder(submissionView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val submissionImageView: ImageView = holder.image.getViewById(R.id.submission_image) as ImageView
        submissionImageView.apply {
            this.clipToOutline = true

            //Incase the list is empty the put the plus icon
            val submissionImage = images[position]
            if(submissionImage != null) {
                submissionImageView.setImageBitmap(Cache.getImageFromMemCache(submissionImage))
            } else {
                submissionImageView.setImageResource(R.drawable.ic_plus_grey)
            }
        }
    }

    override fun getItemCount(): Int = images.size
}

