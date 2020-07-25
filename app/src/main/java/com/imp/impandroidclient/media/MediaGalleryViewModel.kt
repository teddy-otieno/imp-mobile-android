package com.imp.impandroidclient.media

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.FileSystemMedia
import com.imp.impandroidclient.app_state.repos.data.LocalImage

class MediaGalleryViewModel : ViewModel()
{

    override fun onCleared() {
        super.onCleared()
        //Incase the activity was destroyed during loading of images
        //Cancel all the computations
        FileSystemMedia.clean()
    }

    fun getImages(): MutableLiveData<List<LocalImage>> = FileSystemMedia.images
}