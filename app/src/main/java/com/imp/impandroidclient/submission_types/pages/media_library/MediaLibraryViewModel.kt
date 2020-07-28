package com.imp.impandroidclient.submission_types.pages.media_library

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imp.impandroidclient.app_state.repos.FileSystemMedia
import com.imp.impandroidclient.app_state.repos.data.LocalImage

class MediaLibraryViewModel: ViewModel() {

    val selectedImage :MutableLiveData<LocalImage> by lazy { MutableLiveData<LocalImage>() }

    fun getImages(): MutableLiveData<List<LocalImage>> = FileSystemMedia.images
}