/*
 *   Copyright 2020 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.ui.process.base

import androidx.lifecycle.MutableLiveData

/**
 * Wrapper class for handling a processes progress.
 * Use [update] to update its live data objects.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class ProcessProgress {

    val maxPercent: Int = 100
    val progressPercent: MutableLiveData<Int> = MutableLiveData()

    val max: MutableLiveData<Int> = MutableLiveData()
    val current: MutableLiveData<Int> = MutableLiveData()

    init {
        max.postValue(0)
        current.postValue(0)
    }

    /**
     * Update the progress properties.
     */
    fun update(current: Int, from: Int) {
        if (from == 0) {
            return
        }

        this.max.postValue(from)
        this.current.postValue(current)
        progressPercent.postValue((current * maxPercent) / from)
    }
}