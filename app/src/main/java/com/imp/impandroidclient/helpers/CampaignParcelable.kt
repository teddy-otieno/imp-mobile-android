package com.imp.impandroidclient.helpers

import android.os.Parcelable
import com.imp.impandroidclient.dashboards.ui.data_classes.Brand
import com.imp.impandroidclient.dashboards.ui.data_classes.CampaignData
import com.imp.impandroidclient.dashboards.ui.data_classes.Indexed
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class CampaignParcelable(
    val id: Int,
    val brand: Brand,
    val title: String,
    val cover_image: Int,
    val campaign_status: String,
    var call_to_action: String,
    var where_to_find_product: String,
    var about_you: String,
    var start_date: Date,
    var campaign_period: Int,
    var content_wed_love_from_you: String,
    var dos: ArrayList<Indexed>,
    var donts: ArrayList<Indexed>
) : Parcelable

fun convertCampaignToParcelable(campaign: CampaignData, cover_image: Int): CampaignParcelable {
    println(campaign.toString())
    return CampaignParcelable(
        campaign.id,
        campaign.brand,
        campaign.title,
        cover_image,
        campaign.campaign_status,
        campaign.call_to_action,
        campaign.where_to_find_product,
        campaign.about_you,
        campaign.start_date,
        campaign.campaign_period,
        campaign.content_wed_love_from_you,
        campaign.dos,
        campaign.donts
    )
}