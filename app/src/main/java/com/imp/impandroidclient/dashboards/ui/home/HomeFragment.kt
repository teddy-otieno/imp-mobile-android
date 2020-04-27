package com.imp.impandroidclient.dashboards.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.imp.impandroidclient.GlobalApplication
import com.imp.impandroidclient.R
import com.imp.impandroidclient.dashboards.ui.data_classes.CampaignData
import com.imp.impandroidclient.helpers.ImageRequest
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()

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

            println("Was observed")
            println(it)
            home_campaign_list_view.layoutManager = LinearLayoutManager(context)
            home_campaign_list_view.adapter = CampaignComponentAdapter(this, it)
        })
    }
}

class ViewHolder(val context: Fragment, inflator: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflator.inflate(R.layout.frame_campaign_view, parent, false)) {
    private var brandName: TextView
    private var companyAvatar: ImageView

    init {
        brandName = itemView.findViewById(R.id.campaign_component_brand_name)
        companyAvatar = itemView.findViewById(R.id.campaign_component_brand_avatar)
    }

    fun bind(campaign: CampaignData) {
        brandName.text = campaign.title

        context.lifecycleScope.launch {
            val headers: Map<String, String> =
                mapOf("Authorization" to "Bearer " + GlobalApplication.accessToken)
            val request = ImageRequest(
                GlobalApplication.root_path + campaign.cover_image,
                headers as MutableMap<String, String>,
                Response.Listener {
                    companyAvatar.setImageBitmap(it)
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Create a campaign component view
        val inflator = LayoutInflater.from(parent.context)

        return ViewHolder(context, inflator, parent)
    }

    override fun getItemCount() = campaignData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Set new campaignData to that View
        holder.bind(campaignData[position])
    }
}

