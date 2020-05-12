package com.imp.impandroidclient.dashboards.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.imp.impandroidclient.GlobalApplication
import com.imp.impandroidclient.R
import com.imp.impandroidclient.campaign.Campaign
import com.imp.impandroidclient.dashboards.ui.data_classes.CampaignData
import com.imp.impandroidclient.helpers.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import android.util.Pair as UtilPair

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()

    private var campaignAdapter: CampaignComponentAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.homeViewModel.getCampaigns().observe(viewLifecycleOwner, Observer {
            home_campaign_list_view.layoutManager = LinearLayoutManager(context)

            campaignAdapter = CampaignComponentAdapter(this, it)
            home_campaign_list_view.adapter = campaignAdapter!!

        })
    }

    override fun onResume() {
        super.onResume()

        if (campaignAdapter != null) {
            campaignAdapter?.canStart = true
        }
    }
}

class ViewHolder(val context: Fragment, inflator: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflator.inflate(R.layout.frame_campaign_view, parent, false)) {

    private var campaignCoverImage: ImageView
    private var campaignTitle: TextView
    private var campaignDescription: TextView
    private var brandTitle: TextView
    private var cardLayout: ConstraintLayout
    private var coverImage: Int? = null

    init {
        campaignCoverImage = itemView.findViewById(R.id.campaignCoverImage)
        campaignTitle = itemView.findViewById(R.id.campaignTitle)
        campaignDescription = itemView.findViewById(R.id.campaignDescription)
        brandTitle = itemView.findViewById(R.id.campaignBrandName)

        cardLayout = itemView.findViewById(R.id.campaignFrame)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun bind(campaign: CampaignData, contextAdapter: CampaignComponentAdapter) {


        cardLayout.setOnTouchListener { v, event ->
            println("Campaign View Opened")

            if (coverImage != null && contextAdapter.canStart) {
                val intent: Intent = Intent(context.context, Campaign::class.java).also {
                    it.putExtra(
                        "CampaignObject",
                        convertCampaignToParcelable(campaign, coverImage!!)
                    )
                }
                val activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                    context.context as Activity,
                    UtilPair.create(campaignCoverImage as View, "campaignCoverImage"),
                    UtilPair.create(campaignTitle as View, "campaignTitleTransition"),
                    UtilPair.create(campaignDescription as View, "campaignDescriptionTransition")
                )

                println("Alert Activity Started")
                contextAdapter.canStart = false
                context.startActivity(intent, activityOptions.toBundle())
            }

            true
        }

        context.lifecycleScope.launch {
            campaignTitle.text = campaign.title
            campaignDescription.text = campaign.about_you
            brandTitle.text = campaign.brand.brand_name

            if (coverImage != null) {
                campaignCoverImage.setImageBitmap(GlobalApplication.memCache.get(coverImage))
                return@launch
            }

            val headers: Map<String, String> =
                mapOf("Authorization" to "Bearer " + GlobalApplication.accessToken)
            val request = ImageRequest(
                GlobalApplication.root_path + campaign.cover_image,
                headers as MutableMap<String, String>,
                Response.Listener {
                    val cropped = squareCropBitmap(it)
                    val croppedRounded = getRoundedCornerBitmap(
                        cropped,
                        (0.5 * cropped.width).toInt()
                    )
                    //campaignCoverImage.setImageBitmap(croppedRounded)

                    val filled = fillViewBitmap(it, campaignCoverImage)

                    campaignCoverImage.setImageBitmap(filled)
                    campaignCoverImage.clipToOutline = true;

                    val index = GlobalApplication.memCache.size()

                    coverImage = index + 1
                    GlobalApplication.memCache.put(coverImage!!, filled)
                    //campaignCoverImage.setImageBitmap(it)
                },
                Response.ErrorListener {
                    println("Error occured")
                }
            )
            GlobalApplication.httpRequestQueue.add(request)
        }
    }
}

class CampaignComponentAdapter(
    val context: Fragment,
    private val campaignData: ArrayList<CampaignData>
) : RecyclerView.Adapter<ViewHolder>() {

    var canStart: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Create a campaign component view
        val inflator = LayoutInflater.from(parent.context)

        return ViewHolder(context, inflator, parent)
    }

    override fun getItemCount() = campaignData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Set new campaignData to that View
        holder.bind(campaignData[position], this)
    }


}

