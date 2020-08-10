package com.imp.impandroidclient.app_state.repos.data

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred
import java.util.*

//INFO: The other data will be used to filter user
data class CampaignData(
    val id: Int,
    val title: String,
    val brand: Int,
    val cover_image: String,
    val campaign_status: String,
    var call_to_action: String,
    var where_to_find_product: String,
    var about_you: String,
    var start_date: Date,
    var campaign_period: Int,
    var content_wed_love_from_you: String
)

data class Indexed(
    val id: Int,
    val name: String
)

data class MoodBoard(
    val id: Int,
    val file: String
)


data class Brand(
    val id: Int,
    val title: String,
    val image: String,
    val company: Int
)

data class HashTag(
    val id: Int,
    val hashtag: String,
    val channel: String,
    val campaign: Int
)


/**
 * Shared data structure for the dos and donts
 */
data class GenericCampaignItem(
    val id: Int,
    val index: Int,
    val text: String,
    val campaign: Int
)
