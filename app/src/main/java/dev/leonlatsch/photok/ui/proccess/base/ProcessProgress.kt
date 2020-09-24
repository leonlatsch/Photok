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

package dev.leonlatsch.photok.ui.proccess.base

import androidx.lifecycle.MutableLiveData

class ProcessProgress {

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