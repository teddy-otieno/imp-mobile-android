package com.imp.impandroidclient.submission_types.choose_media_pages.media_library

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imp.impandroidclient.R
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.app_state.repos.FileSystemMedia
import com.imp.impandroidclient.app_state.repos.data.LocalImage
import com.imp.impandroidclient.submission_types.ChooseMediaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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

    val activityViewModel : ChooseMediaViewModel by activityViewModels()
    private val libViewModel : MediaLibraryViewModel by activityViewModels()

    private lateinit var mediaGrid : RecyclerView
    private lateinit var imagePreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {

            FileSystemMedia.loadImages()

        } ?: throw IllegalStateException("ACTIVITY NOT EXPECTED TO BE NULL")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val libraryView = inflater.inflate(R.layout.fragment_media_library, container, false)
        val ref = this

        mediaGrid = libraryView.findViewById<RecyclerView>(R.id.media_grid).apply {
            layoutManager = GridLayoutManager(context, 4)


            val images = libViewModel.getImages().value ?: mutableListOf()

            adapter = MediaViewAdapter(ref, images)

            libViewModel.newImage().observe(viewLifecycleOwner, Observer {
                if(it != null)
                    (adapter as MediaViewAdapter).addItem(it)
            })
        }

        imagePreview = libraryView.findViewById(R.id.preview)

        activityViewModel.selectedImage.observe(this.viewLifecycleOwner, Observer {
            activity?.let { activity ->
                lifecycleScope.launch(Dispatchers.Main) {
                    ResourceManager.getLocalImage(activity.contentResolver, it) {
                        imagePreview.setImageBitmap(it)
                    }
                }
            }
        })

        return libraryView
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = MediaLibrary().apply { }
    }
}

private class MediaViewHolder(
    private val parentFragment: MediaLibrary,
    itemView: View,
    parent: ViewGroup
): RecyclerView.ViewHolder(itemView) {

    val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    fun bind(item: LocalImage) {

        //Cancel previous scope
        try {
            scope.cancel()
        } catch (e: IllegalStateException) {

            Log.w("COROUTINE", "No job was found in this scope")
        }

        parentFragment.activity?.let { activity ->
            val thumbnailView = itemView.findViewById<ImageView>(R.id.thumbnail_view).apply{
                layoutParams.height = layoutParams.width
            }

            itemView.setOnClickListener {
                parentFragment.activityViewModel.selectedImage.value = item
            }

            parentFragment.lifecycleScope.launch(Dispatchers.Main) {
                ResourceManager.getLocalImage(activity.contentResolver, item) { bitmap ->
                    thumbnailView.setImageBitmap(bitmap)
                }
            }

        } ?: throw IllegalStateException("")

        parentFragment.activityViewModel.selectedImage.observe(parentFragment.viewLifecycleOwner, Observer {
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

private class MediaViewAdapter(
    private val parentFragment: MediaLibrary,
    private val media_objects :List<LocalImage>
): RecyclerView.Adapter<MediaViewHolder>() {

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

    fun addItem(item: LocalImage) {
        (media_objects as MutableList) += item
        notifyItemInserted(media_objects.size - 1)
    }
}