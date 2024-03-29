package com.imp.impandroidclient.dashboards.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.imp.impandroidclient.R
import java.lang.IllegalStateException

class Library : Fragment()
{

    private val dashboardViewModel: LibraryViewModel by viewModels()
    private lateinit var submissionView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val root = inflater.inflate(R.layout.fragment_library, container, false)
        setupFrag(root)
        setUpObservers(root)
        setUpListeners(root)
        return root
    }

    private fun setupFrag(rootView: View)
    {
        submissionView = rootView.findViewById(R.id.submissions_view)
        submissionView.layoutManager = LinearLayoutManager(activity)
    }

    private fun setUpObservers(rootView: View)
    {
        val submissionsView: RecyclerView = rootView.findViewById(R.id.submissions_view)

        activity?.let {
            dashboardViewModel.combinedSubmission.observe(it, Observer {submissions ->
                submissionsView.adapter = SubmissionsViewAdapter(submissions)
            })
        } ?: throw IllegalStateException("Activity is not supposed to be null")

    }

    private fun setUpListeners(rootView: View)
    {
        
    }
}

class SubmissionViewHolder(private val view: View): RecyclerView.ViewHolder(view)
{
    fun bind(submission: CombinedSubmission)
    {
        val submissionCaption: TextView = view.findViewById(R.id.submission_caption)
        val submissionRate: TextView = view.findViewById(R.id.rates)
        val submissionStatus: TextView = view.findViewById(R.id.status)
        val submissionImage: ImageView = view.findViewById(R.id.image)

        submission.caption?.let {
            if(it.length > 50)
                submissionCaption.text = "${it.subSequence(0, 50)}..."
            else
                submissionCaption.text = it
        }

        submissionRate.text = "FEES: ${submission.rate}"
        submissionStatus.text = submission.status?.toString() ?: "DRAFT"
        submission.thumbnail?.let {
            submissionImage.setImageBitmap(it)
        }

        view.setOnClickListener {
            TODO("Add onclick listener")
        }
    }
}

class SubmissionsViewAdapter(private val submissions: List<CombinedSubmission>) : RecyclerView.Adapter<SubmissionViewHolder>()
{
    override fun getItemCount(): Int = submissions.size

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int)
    {
        holder.bind(submissions[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder
    {
        val view: MaterialCardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_post_submission_card, parent, false) as MaterialCardView

        return SubmissionViewHolder(view)
    }
}