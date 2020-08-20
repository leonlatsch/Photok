package dev.leonlatsch.photok.ui.importing

import androidx.lifecycle.MutableLiveData

class ImportProgress {

    val max: Int = 100
    val progress: MutableLiveData<Int> = MutableLiveData()

    fun update(current: Int, from: Int) {
        progress.postValue((current * max) / from)
    }
}