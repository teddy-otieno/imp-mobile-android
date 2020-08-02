package com.imp.impandroidclient.dashboards.ui.home

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.imp.impandroidclient.CAMPAIGN_ID
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.repos.CampaignRepository
import com.imp.impandroidclient.app_state.repos.data.CampaignData
import com.imp.impandroidclient.campaign.CampaignActivity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import android.util.Pair as UtilPair


class HomeFragment : Fragment() {

    lateinit var homeViewModel: HomeViewModel

    private var campaignAdapter: CampaignComponentAdapter? = null
    private lateinit  var activityToolBar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        activityToolBar = activity?.let{
            homeViewModel = ViewModelProvider(it).get(HomeViewModel::class.java)
            retainInstance = true

            it.findViewById<MaterialToolbar>(R.id.tool_bar)
        } ?: throw IllegalStateException("ACTIVITY SHOULD BE NULL")

        if(!homeViewModel.menuSet) {
            activityToolBar.title = resources.getString(R.string.home)
            activityToolBar.inflateMenu(R.menu.home_fragment_toolbar_menu)

            activityToolBar.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.search -> {
                        Log.i("TAG", "SEARCH CLICKED")
                        true
                    }

                    else -> false
                }
            }
            homeViewModel.menuSet = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

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
        campaignAdapter?.canStart = campaignAdapter != null
    }
}

private class ViewHolder(val context: HomeFragment, inflator: LayoutInflater, parent: ViewGroup)
: RecyclerView.ViewHolder(inflator.inflate(R.layout.frame_campaign_view, parent, false)) {

    private val campaignCoverImage: ImageView = itemView.findViewById(R.id.card_campaign_cover_image)
    private val campaignTitle: TextView = itemView.findViewById(R.id.card_campaign_title)
    private val brandTitle: TextView = itemView.findViewById(R.id.card_brand_name)
    private val brandAvatar: ImageView = itemView.findViewById(R.id.card_brand_avatar)

    fun bind(contextAdapter: CampaignComponentAdapter, campaign: CampaignData, index: Int) {

        itemView.setOnClickListener {

            if (contextAdapter.canStart) {

                val bundle = Bundle().apply {
                    putInt(CAMPAIGN_ID, campaign.id)
                };

                val intent: Intent = Intent(context.context, CampaignActivity::class.java).apply {
                    putExtras(bundle)
                }

                val activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                    context.activity,
                    UtilPair.create(campaignCoverImage as View, "campaign_cover_image"),
                    UtilPair.create(itemView, "campaign_container"),
                    UtilPair.create(campaignTitle as View, "campaign_title")
                    //UtilPair.create(brandTitle as View, "brand_title")
                )


                contextAdapter.canStart = false
                context.startActivity(intent, activityOptions.toBundle())
            }

        }

        campaignTitle.text = campaign.title



        ResourceManager.onLoadImage(campaign.cover_image) {
            campaignCoverImage.setImageBitmap(it)
        }

        context.homeViewModel.brands().observe(context.viewLifecycleOwner, Observer {
            val brand = it.find { it.id == campaign.brand }

            brand?.let {
                ResourceManager.onLoadImage(it.image){
                    brandAvatar.setImageBitmap(it)
                    brandAvatar.clipToOutline = true
                }

                brandTitle.text = brand.title
            }

        })

    }

}

private class CampaignComponentAdapter(
    val context: HomeFragment,
    private val campaignData: List<CampaignData>
) : RecyclerView.Adapter<ViewHolder>() {

    var canStart: Boolean = true
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(context, LayoutInflater.from(parent.context), parent)

    override fun getItemCount() = campaignData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Set new campaignData to that View
        holder.bind(this, campaignData[position], position)
    }
}

