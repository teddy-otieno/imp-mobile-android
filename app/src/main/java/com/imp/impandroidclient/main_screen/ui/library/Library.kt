package com.imp.impandroidclient.main_screen.ui.library

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.imp.impandroidclient.R
import com.imp.impandroidclient.SUBMISSION_ID
import com.imp.impandroidclient.app_state.ResourceManager
import com.imp.impandroidclient.main_screen.DashBoardFragment
import com.imp.impandroidclient.submission_types.PostSubmissionView
import com.imp.impandroidclient.submission_types.SubmissionApproved
import kotlinx.coroutines.*
import java.lang.IllegalStateException

class Library : DashBoardFragment() {

    private val dashboardViewModel: LibraryViewModel by activityViewModels()
    private lateinit var submissionView: RecyclerView

    override fun updateToolBar(view: MaterialToolbar) {
        val materialToolbar: MaterialToolbar = activity?.findViewById(R.id.tool_bar)
            ?: throw IllegalStateException("ACTIVITY WAS NULL")

        materialToolbar.title = resources.getString(R.string.library)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFrag(view)
        setUpObservers(view)
        setUpListeners(view)
    }

    private fun setupFrag(rootView: View) {
        submissionView = rootView.findViewById(R.id.submissions_view)
        submissionView.layoutManager = LinearLayoutManager(activity)

    }

    private fun setUpObservers(rootView: View) {

        val fragmentRef = this
        activity?.let {
            dashboardViewModel.combinedSubmission.observe(it, Observer {submissions ->
                submissionView.adapter = SubmissionsViewAdapter(submissions, fragmentRef)
            })
        } ?: throw IllegalStateException("Activity is not supposed to be null")
    }

    private fun setUpListeners(rootView: View) {
    }
}


private class SubmissionViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

    suspend  fun bind(submission: CombinedSubmission, library: Library) = withContext(Dispatchers.Main) {
        val submissionCaption: TextView = view.findViewById(R.id.submission_caption)
        val submissionStatus: TextView = view.findViewById(R.id.status)
        val submissionImage: ImageView = view.findViewById(R.id.image)

        submission.caption?.let {
            if(it.length > 50)
                submissionCaption.text = "${it.subSequence(0, 50)}..."
            else
                submissionCaption.text = it
        }
        submissionStatus.text = submission.status?.toString() ?: "DRAFT"

        submission.url?.let { url ->
            ResourceManager.onLoadImage(url) { resultImage ->

                withContext(Dispatchers.Main) {
                    if(resultImage == null) {
                        submissionImage.setImageBitmap(null)

                    } else {
                        //Note(teddy) Cache when possible
                        val scale = 4.0
                        val scaledBitmap = Bitmap.createScaledBitmap(
                                resultImage,
                                (resultImage.width / scale).toInt(),
                                (resultImage.height / scale).toInt(),
                                true
                            )
                        submissionImage.setImageBitmap(scaledBitmap)
                    }
                }
            }

        }

        view.setOnClickListener {
            val activityClass = when(submission.type) {
                SubmissionType.POST -> {

                    if(submission.status == "APPROVED")
                        SubmissionApproved::class.java
                    else
                        PostSubmissionView::class.java

                }

                else -> Library::class.java
            }
            val intent = Intent(library.activity, activityClass).apply {
                putExtra(SUBMISSION_ID, submission.submissionId)
            }

            //TODO(teddy) Maybe ask activity to a flag if the selected item was modified \
            //Then update the item on this list
            library.startActivity(intent)
        }
    }
}

private data class BindJob(val id: Int, val job: Job)

private class SubmissionsViewAdapter(
    private val submissions: List<CombinedSubmission>,
    private val library: Library
) : RecyclerView.Adapter<SubmissionViewHolder>() {

    val buffer: MutableList<BindJob> = mutableListOf()

    override fun getItemCount(): Int = submissions.size

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {

        val previous = buffer.find { it.id == position }

        previous?.job?.cancel()

        val job = library.lifecycleScope.launch(Dispatchers.Main) {
            holder.bind(submissions[position], library)
        }

        if(previous != null) {
            buffer[buffer.indexOf(previous)] = BindJob(position, job)
        } else {
            buffer.add(BindJob(position, job))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_post_submission_card, parent, false)

        return SubmissionViewHolder(view)
    }
}