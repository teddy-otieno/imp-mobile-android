package com.imp.impandroidclient.media

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.FileSystemMedia
import com.imp.impandroidclient.app_state.repos.data.LocalImage

class MediaGalleryViewModel : ViewModel()
{
    private val fileSystemMediaSubscription: FileSystemMedia = FileSystemMedia.getInstance()

    override fun onCleared()
    {
        super.onCleared()
        //Incase the activity was destroyed during loading of images
        //Cancel all the computations
        fileSystemMediaSubscription.clean()
    }

    fun getImages(): MutableLiveData<List<LocalImage>> = fileSystemMediaSubscription.images
}