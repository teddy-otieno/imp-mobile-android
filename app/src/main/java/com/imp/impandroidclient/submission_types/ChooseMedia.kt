package com.imp.impandroidclient.submission_types

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.imp.impandroidclient.R
import com.imp.impandroidclient.submission_types.pages.camera_capture.PhotoCapture
import com.imp.impandroidclient.submission_types.pages.media_library.MediaLibrary
import kotlinx.android.synthetic.main.activity_choose_media.*
import java.lang.IllegalStateException


private const val READ_STORAGE_PERMISSIONS = 0x01

class ChooseMedia : AppCompatActivity() , ActivityCompat.OnRequestPermissionsResultCallback {

    private val viewPagerAdapter: ChooseMediaViewPageAdapter = ChooseMediaViewPageAdapter(supportFragmentManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_media)

        if(Build.VERSION.SDK_INT >= 23) {

            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                setUp()
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    Snackbar.make(container, R.string.storage_permission_required, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.ok) {
                            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_STORAGE_PERMISSIONS)
                        }

                    setUp()
                } else {
                    //NOTE(teddy) Request a retry
                    Snackbar.make(container, R.string.storage_permission_unavailable, Snackbar.LENGTH_SHORT)

                }
            }
        } else {
            setUp()
        }
    }

    private fun setUp() {
        media_chooser_pager.adapter = viewPagerAdapter
        tab_option.setupWithViewPager(media_chooser_pager)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(requestCode == READ_STORAGE_PERMISSIONS) {
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