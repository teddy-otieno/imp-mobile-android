package com.imp.impandroidclient.dashboards.ui.data_classes

import java.util.*

//INFO: The other data will be used to filter user
data class CampaignData(
    val id: Int,
    val title: String,
    val cover_image: String,
    val campaign_status: String,

    var call_to_action: String? = null,
    var mood_boards: ArrayList<MoodBoard>? = null,
    var where_to_find_product: String? = null,
    var about_you: String? = null,
    var categories: ArrayList<String>? = null,
    var start_date: Date? = null,
    var campaign_period: Int? = null,
    var content_wed_love_from_you: String? = null,
    var dos: ArrayList<Indexed>? = null,
    var donts: ArrayList<Indexed>? = null
)

data class Indexed(
    val id: Int,
    val name: String
)

data class MoodBoard(
    val id: Int,
    val file: String
)