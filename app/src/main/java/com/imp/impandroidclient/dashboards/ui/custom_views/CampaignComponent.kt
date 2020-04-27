package com.imp.impandroidclient.dashboards.ui.custom_views

import android.app.Activity
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.imp.impandroidclient.R
import com.imp.impandroidclient.dashboards.ui.data_classes.CampaignData
import kotlinx.android.synthetic.main.frame_campaign_view.view.*

class CampaignComponentView @JvmOverloads constructor(
    context: Activity,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attributeSet, defStyle) {

    init {
        addView(View.inflate(context, R.layout.frame_campaign_view, null))
    }

    fun setData(campaignData: CampaignData) {
        campaign_component_brand_name.text = "Test"
        //campaign_component_campaign_cover_image.setImageBitmap(campaignData.cover_image)
        campaign_component_campaign_title.text = campaignData.title
    }
}