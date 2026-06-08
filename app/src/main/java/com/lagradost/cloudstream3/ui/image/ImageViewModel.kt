package com.lagradost.cloudstream3.ui.image

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lagradost.cloudstream3.utils.ImageLink

class ImageViewModel : ViewModel() {
    private val _imageLinks = MutableLiveData<List<ImageLink>>()
    val imageLinks: LiveData<List<ImageLink>> = _imageLinks

    fun setImages(links: List<ImageLink>) {
        _imageLinks.postValue(links)
    }
}