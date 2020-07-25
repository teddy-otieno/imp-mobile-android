package com.imp.impandroidclient.media

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.repos.FileSystemMedia
import com.imp.impandroidclient.app_state.repos.data.LocalImage
import com.imp.impandroidclient.submission_types.post.SELECT_IMAGE
import kotlinx.android.synthetic.main.activity_media_gallery.*


class MediaGallery : AppCompatActivity()
{

    companion object
    {
        const val REQUEST_READ_EXTERNAL_MEDIA = 2
    }

    private val viewModel: MediaGalleryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_gallery)

        init()
        setUpListeners()
    }

    private fun init()
    {
        media_grid.layoutManager = GridLayoutManager(this, 3)

        //Requesting permissions from android 6
        if(Build.VERSION.SDK_INT >= 23) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    Toast.makeText(this, "Permission to display images on the device", Toast.LENGTH_LONG).show() //FIXME:
                }
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_MEDIA)
            }
            else
            {
                setUpObservers()
            }
        }

    }

    private fun setUpListeners()
    {
        val toolBar: MaterialToolbar = findViewById(R.id.gallery_back_navigation)
        toolBar.setNavigationOnClickListener {
            endRoutine()
        }
    }

    private fun setUpObservers()
    {
        FileSystemMedia.application = application

        viewModel.getImages().observe(this, Observer {
            val viewAdapter = GalleryAdapter(this, it)

            media_grid.adapter = viewAdapter
        })
    }

    private fun endRoutine()
    {
        val resultIntent = Intent()
        resultIntent.putExtra("image", "ImageId")
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray)
    {
        if(requestCode == REQUEST_READ_EXTERNAL_MEDIA)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                setUpObservers()
            }
            else
            {
                Toast.makeText(this, "Permission was not granted", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == SELECT_IMAGE)
        {
            if(resultCode == RESULT_OK)
            {
                data?.run{
                    //TODO(tedd) pass the result back to the caller
                    setResult(Activity.RESULT_OK, this)
                    finish()
                } ?: throw IllegalStateException("data passed from ImageSelect activity should not be null")
            }
        }
    }
}

private class GalleryViewHolder(val image: ImageView) : RecyclerView.ViewHolder(image)

private class GalleryAdapter(private val activity: AppCompatActivity, private val images: List<LocalImage>)
    : RecyclerView.Adapter<GalleryViewHolder>()
{
    override fun getItemCount(): Int  = images.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder
    {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

        val imageWidth = (displayMetrics.widthPixels / 3) as Int
        val image = ImageView(activity).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val layoutParams = LinearLayout.LayoutParams(imageWidth, imageWidth)
        image.layoutParams = layoutParams

        return GalleryViewHolder(image)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int)
    {
        if(Build.VERSION.SDK_INT > 29)
        {
            holder.image.setImageBitmap(
                activity.contentResolver.loadThumbnail(images[position].contentUri,
                    Size(holder.image.height, holder.image.width), null ))
        }
        else
        {
            val bitmapOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            holder.image.setImageBitmap(
                MediaStore.Images.Thumbnails.getThumbnail(
                    activity.contentResolver,
                    images[position].imageIndex,
                    MediaStore.Images.Thumbnails.MINI_KIND, bitmapOptions)
            )
        }

        holder.image.setOnClickListener {
            it.transitionName = "image"
            val intent = Intent(activity, ImageSelector::class.java).apply {
                putExtra("IMAGE", images[position].contentUri)
            }

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, it as View, "image")
            activity.startActivityForResult(intent , SELECT_IMAGE, options.toBundle())
        }
    }
}