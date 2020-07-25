package com.imp.impandroidclient.dashboards.ui.library

import android.graphics.Bitmap
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.imp.impandroidclient.app_state.repos.PostSubmissionRepo
import com.imp.impandroidclient.app_state.repos.data.PostSubmission
import com.imp.impandroidclient.app_state.repos.data.SubmissionStatus
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.util.*

class LibraryViewModel : ViewModel()
{
    private val observer: Observer<MutableList<MutableLiveData<PostSubmission>>>

    val combinedSubmission: MutableLiveData<List<CombinedSubmission>> = MutableLiveData( mutableListOf())

    init {
        observer = Observer { rawSubmissions ->
            /*
                Note(teddy) append this to the combinedSubmissions
             */
            viewModelScope.launch(Dispatchers.Default) {
                val list = rawSubmissions
                val submissions = combinedSubmission.value
                    ?: throw IllegalStateException("Expected a value in the combined Submission")

                (submissions as MutableList).addAll(list.map {
                    it.value?.let { submission ->
                        CombinedSubmission(
                            SubmissionType.POST,
                            submission.id,
                            submission.campaignId,
                            submission.postCaption,
                            submission.fee,
                            submission.timeOfSubmission,
                            submission.status,
                            submission.image_url
                        )
                    } ?: throw IllegalStateException("Expected value inside the post submission")
                })

                //FIXME(teddy) on the server side submission time is not implemented
                submissions.sortBy {
                    it.time
                }

                combinedSubmission.postValue(submissions)
            }
        }

        PostSubmissionRepo.submissions.observeForever(observer)
    }

    override fun onCleared() {
        super.onCleared()

        PostSubmissionRepo.submissions.removeObserver(this.observer)
    }
}


enum class SubmissionType
{
    POST,
    CAROUSEL,
}

/**
 * Note(teddy) Consider using inheritance
 * Later when we integrate with videos and other types of submission
 * TODO(teddy) When handling submissions is best to avoid null, create a temporary store for the data
 *
 *
 * @warning
 * Note(teddy) To add support for views!!!
 *
 * @param url holds the key of the bitmap in the Image Cache
 */
data class CombinedSubmission(
    val type: SubmissionType,
    val submissionId: Int,
    val campaignId: Int,
    val caption: String?,
    val rate: Int?,
    val time: Date?,
    val status: SubmissionStatus?,
    val url: String? = null
)