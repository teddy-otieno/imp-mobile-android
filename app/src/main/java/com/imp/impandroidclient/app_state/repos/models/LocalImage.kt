package com.imp.impandroidclient.app_state.repos.models

import android.net.Uri

data class LocalImage(val imageID: Long, val contentUri: Uri) {

    override fun toString(): String =  contentUri.toString()
}