package dev.leonlatsch.photok.ui.importing

import androidx.lifecycle.MutableLiveData

class ImportProgress {

    val maxPercent: Int = 100
    val progressPercent: MutableLiveData<Int> = MutableLiveData()

    val max: MutableLiveData<Int> = MutableLiveData()
    val current: MutableLiveData<Int> = MutableLiveData()

    init {
        max.postValue(0)
        current.postValue(0)
    }

    fun update(current: Int, from: Int) {
        this.max.postValue(from)
        this.current.postValue(current)
        progressPercent.postValue((current * maxPercent) / from)
    }
}