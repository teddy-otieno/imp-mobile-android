package com.imp.impandroidclient.campaign

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.lifecycle.*
import androidx.navigation.ActivityNavigator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imp.impandroidclient.CAMPAIGN_ID
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.repos.CampaignRepository
import com.imp.impandroidclient.app_state.repos.data.CampaignData
import com.imp.impandroidclient.app_state.repos.data.GenericCampaignItem
import com.imp.impandroidclient.app_state.repos.data.MoodBoard
import com.imp.impandroidclient.submission_types.ChooseMedia
import kotlinx.android.synthetic.main.activity_campaign.*
import kotlinx.android.synthetic.main.layout_campaign_info_details.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        viewModel.loadMoodBoards()

        val layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        moodboards_view.layoutManager = layoutManager

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

            lifecycleScope.launch(Dispatchers.Main) {
                ResourceManager.onLoadImage(campaign.cover_image) {
                    detailed_campaignCoverImage.setImageBitmap(it)
                }
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

        val activity = this
        viewModel.moodboards.observe(this, Observer {

            lifecycleScope.launch(Dispatchers.Main) {
                moodboards_view.adapter = MoodBoardAdapter(activity, it)
            }
        })
    }

    override fun onBackPressed() {
        information_view.visibility = View.INVISIBLE
        finishAfterTransition()
    }
}

private class MoodBoardViewHolder(val view: ImageView): RecyclerView.ViewHolder(view) {

    suspend fun bind(item: MoodBoard) {

        ResourceManager.onLoadImage(item.image) {
            (itemView as ImageView).setImageBitmap(it)
        }
    }
}

fun getPixelValue(context: Context, dimens: Float): Int {
    val resources = context.resources

    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dimens,
        resources.displayMetrics
    ).toInt()
}

private class MoodBoardAdapter(val context: CampaignActivity, val moodBoards: List<MoodBoard>)
    : RecyclerView.Adapter<MoodBoardViewHolder>() {

    override fun getItemCount(): Int = moodBoards.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodBoardViewHolder {

        //val view = inflater.inflate(R.layout.item_moodboard, null, false) as ImageView


        val height = 200f
        val width = 150f

        val layout = ViewGroup.MarginLayoutParams(
            getPixelValue(context, width),
            getPixelValue(context, height)
        ).apply {
            marginStart = getPixelValue(context, 4f)
            marginEnd = getPixelValue(context, 4f)
        }

        val view = ImageView(context).apply {
            layoutParams = layout
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        return MoodBoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodBoardViewHolder, position: Int) {
        context.lifecycleScope.launch {
            holder.bind(moodBoards[position])
        }
    }
}


class CampaignViewModelFactory(private val campaignId: Int) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(CampaignViewModel::class.java)) {
            return CampaignViewModel(campaignId) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class CampaignViewModel(val campaignId: Int) : ViewModel() {

    val campaignData: MutableLiveData<CampaignData> = MutableLiveData(CampaignRepository.getCampaignOfId(campaignId))
    val dos  get() = CampaignRepository.dosData
    val donts get() = CampaignRepository.dontsData
    val moodboards get() = CampaignRepository.moodBoards

    fun loadDosAndDonts() = CampaignRepository.loadDosAndDonts(campaignId)

    fun loadMoodBoards() {
        viewModelScope.launch(Dispatchers.Main) {
            CampaignRepository.loadMoodBoards(campaignId)
        }
    }
    fun updateCampaign(campaign: CampaignData) {
        CampaignRepository.updateCampaign(campaign, campaignId)
        campaignData.postValue(CampaignRepository.getCampaignOfId(campaignId))
    }
}
