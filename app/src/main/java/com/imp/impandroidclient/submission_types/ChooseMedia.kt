package com.imp.impandroidclient.submission_types

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.imp.impandroidclient.CAMPAIGN_ID
import com.imp.impandroidclient.IMAGE_URI
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.repos.data.LocalImage
import com.imp.impandroidclient.app_state.repos.data.PostSubmission
import com.imp.impandroidclient.submission_types.pages.camera_capture.PhotoCapture
import com.imp.impandroidclient.submission_types.pages.media_library.MediaLibrary
import kotlinx.android.synthetic.main.activity_choose_media.*
import java.lang.IllegalStateException
import kotlin.properties.Delegates


private const val READ_STORAGE_PERMISSION_ID = 0x01


/**
 * Expects [CAMPAIGN_ID] in the intent bundle
 * Activity throws IllegalStateException when its absent
 */
class ChooseMedia :AppCompatActivity() , ActivityCompat.OnRequestPermissionsResultCallback {

    private var campaignId: Int by Delegates.notNull()

    private val viewPagerAdapter: ChooseMediaViewPageAdapter
            = ChooseMediaViewPageAdapter(supportFragmentManager)

    private val model: ChooseMediaViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_media)

        val bundle: Bundle = intent.extras ?: throw IllegalStateException("EXPECTED A BUNDLE")
        campaignId = bundle.getInt(CAMPAIGN_ID)

        if(Build.VERSION.SDK_INT >= 23) {

            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

                setUp()

            } else {

                if(shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    Snackbar.make(
                        container,
                        R.string.storage_permission_required,
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(R.string.ok) {

                        requestPermissions(
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            READ_STORAGE_PERMISSION_ID
                        )
                    }.show()

                    setUp()
                } else {

                    //NOTE(teddy) Request a retry
                    Snackbar.make(
                        container,
                        R.string.storage_permission_unavailable,
                        Snackbar.LENGTH_SHORT
                    ).show()

                }
            }
        } else {
            setUp()
        }
    }

    private fun setUp() {
        media_chooser_pager.adapter = viewPagerAdapter
        tab_option.setupWithViewPager(media_chooser_pager)

        choose_media_toolbar.setOnMenuItemClickListener {

            when(it.itemId) {

                R.id.next_button -> {
                    val bundle = Bundle().apply {
                        val imageUri = model.selectedImage.value?.contentUri
                            ?: throw IllegalStateException("IMAGE SHOULD NOT SELECTED")

                        putParcelable(IMAGE_URI, imageUri)
                        putInt(CAMPAIGN_ID, campaignId)
                    }

                    val intent = Intent(this, PostSubmissionDetails::class.java).apply {
                        putExtras(bundle)
                    }

                    startActivity(intent)
                    true
                }

                else -> false
            }

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(requestCode == READ_STORAGE_PERMISSION_ID) {
            if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(container, R.string.storage_permission_granted, Snackbar.LENGTH_SHORT)
                setUp()
            }
        }
    }
}

private class ChooseMediaViewPageAdapter(fragmentManager: FragmentManager):
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> MediaLibrary.newInstance()
            1 -> PhotoCapture.newInstance()

            else -> throw IllegalStateException("UNEXPECTED ITEM INDEX")
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {

        return when(position) {
            0 -> "Library"
            1 -> "Photo"

            else -> throw IllegalStateException("UNEXPECTED ITEM INDEX")
        }
    }

}

class ChooseMediaViewModel : ViewModel() {
    val selectedImage : MutableLiveData<LocalImage> by lazy {
        MutableLiveData<LocalImage>()
    }

    val selectedSubmission: MutableLiveData<PostSubmission> by lazy {
        MutableLiveData<PostSubmission>()
    }
}