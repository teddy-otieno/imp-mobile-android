package com.imp.impandroidclient.submission_types

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.imp.impandroidclient.R
import com.imp.impandroidclient.media.MediaGallery

class MediaChoiceBottomSheet(private val parent: AppCompatActivity) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?  {
        val view = inflater.inflate(R.layout.camera_or_gallery_dialog, null)
        val postSubmissionOption: LinearLayout = view.findViewById(R.id.post_selection)

        postSubmissionOption.setOnClickListener {
            val intent = Intent(parent, MediaGallery::class.java)
            parent.startActivity(intent)
        }

        return view
    }
}