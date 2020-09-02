package com.imp.impandroidclient.submission_types.choose_media_pages.media_library

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.FileSystemMedia
import com.imp.impandroidclient.app_state.repos.models.LocalImage

class MediaLibraryViewModel: ViewModel() {


    fun getImages(): MutableLiveData<MutableList<LocalImage>> = FileSystemMedia.images
    fun newImage(): MutableLiveData<LocalImage> = FileSystemMedia.newImage
}