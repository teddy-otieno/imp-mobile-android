package com.imp.impandroidclient.submission_types.post

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.imp.impandroidclient.R
import kotlinx.android.synthetic.main.activity_post.*

class Post : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        setUpListeners()
    }

    private fun setUpListeners() {
        post_back_button.setOnClickListener {
            finish()
        }
    }
}
