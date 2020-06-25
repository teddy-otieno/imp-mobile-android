package com.imp.impandroidclient.dashboards.ui.library

import android.graphics.Bitmap
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.imp.impandroidclient.app_state.repos.PostSubmissionRepo
import com.imp.impandroidclient.app_state.repos.data.PostSubmission
import com.imp.impandroidclient.app_state.repos.data.SubmissionStatus
import com.imp.impandroidclient.submission_types.post.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.util.*

class LibraryViewModel : ViewModel()
{
    private val observer: Observer<MutableList<MutableLiveData<PostSubmission>>>

    private val postSubmissionRepo: PostSubmissionRepo = PostSubmissionRepo.getInstance()
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
                    it.value?.run {
                        CombinedSubmission(this.postCaption, this.fee , this.image, this.timeOfSubmission, this.status)
                    } ?: throw IllegalStateException("Expected value inside the post submission")
                })

                //FIXME(teddy) on the server side submission time is not implemented
                submissions.sortBy {
                    it.time
                }

                combinedSubmission.postValue(submissions)
            }
        }

        postSubmissionRepo.submissions.observeForever(observer)
    }

    override fun onCleared() {
        super.onCleared()

        postSubmissionRepo.submissions.removeObserver(this.observer)
    }
}


/**
 * Note(teddy) Consider using inheritance
 * Later when we integrate with videos and other types of submission
 * TODO(teddy) When handling submissions is best to avoid null, create a temporary store for the data
 */
data class CombinedSubmission(
    val caption: String?,
    val rate: Int?,
    val thumbnail: Bitmap?,
    val time: Date?,
    val status: SubmissionStatus?
)