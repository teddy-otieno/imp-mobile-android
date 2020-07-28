package com.imp.impandroidclient.submission_types.pages.media_library

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.repos.FileSystemMedia
import com.imp.impandroidclient.app_state.repos.data.LocalImage
import java.lang.IllegalStateException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MediaLibrary.newInstance] factory method to
 * create an instance of this fragment.
 */
class MediaLibrary : Fragment() {

    lateinit var libViewModel : MediaLibraryViewModel
    private lateinit var mediaGrid : RecyclerView
    private lateinit var imagePreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        libViewModel = ViewModelProvider(this).get(MediaLibraryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        FileSystemMedia.load_images()
        // Inflate the layout for this fragment
        val libraryView = inflater.inflate(R.layout.fragment_media_library, container, false)
        val ref = this

        mediaGrid = libraryView.findViewById<RecyclerView>(R.id.media_grid).apply {
            layoutManager = GridLayoutManager(context, 4)

            libViewModel.getImages().observe(viewLifecycleOwner, Observer {
                adapter = MediaViewAdapter(ref, it)
            })
        }

        imagePreview = libraryView.findViewById(R.id.preview)

        libViewModel.selectedImage.observe(this.viewLifecycleOwner, Observer {
            activity?.let { activity ->
                ResourceManager.getLocalImage(activity.contentResolver, it) {
                    imagePreview.setImageBitmap(it)
                }
            }
        })

        return libraryView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MediaLibrary.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = MediaLibrary().apply { }
    }
}

private class MediaViewHolder(private val parentFragment: MediaLibrary, itemView: View, parent: ViewGroup)
    : RecyclerView.ViewHolder(itemView) {

    fun bind(item: LocalImage) {

        parentFragment.activity?.let { activity ->
            val thumbnailView = itemView.findViewById<ImageView>(R.id.thumbnail_view).apply{
                layoutParams.height = layoutParams.width
            }

            itemView.setOnClickListener {
                parentFragment.libViewModel.selectedImage.value = item
            }

            ResourceManager.getLocalImage(activity.contentResolver, item) { bitmap ->
                thumbnailView.setImageBitmap(bitmap)
            }

        } ?: throw IllegalStateException("")

        parentFragment.libViewModel.selectedImage.observe(parentFragment.viewLifecycleOwner, Observer {
            val selectedItemRadio = itemView.findViewById<RadioButton>(R.id.selected_item).apply {
                if(item.contentUri == it.contentUri) {
                    visibility =  View.VISIBLE
                    isChecked = true
                } else {
                    visibility = View.GONE
                    isChecked = false
                }
            }

        })

    }
}

private class MediaViewAdapter(private val parentFragment: MediaLibrary, private val media_objects :List<LocalImage>): RecyclerView.Adapter<MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {

        val viewItem = LayoutInflater.from(parentFragment.context)
            .inflate(R.layout.layout_media_thumbnail, parent, false)

        val height = parent.measuredWidth / 4
        viewItem.layoutParams.height = height

        return MediaViewHolder(parentFragment, viewItem, parent)
    }

    override fun getItemCount(): Int = media_objects.size


    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(media_objects[position])
    }
}