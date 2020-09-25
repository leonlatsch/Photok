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

package dev.leonlatsch.photok.ui.proccess

import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.ui.proccess.base.BaseProcessViewModel
import dev.leonlatsch.photok.ui.proccess.base.ProcessState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ImportViewModel : BaseProcessViewModel(){

    override fun process() = viewModelScope.launch {
        delay(1000)
        processState.postValue(ProcessState.PROCESSING)
        for (i in 0..100) {
            delay(100)
            progress.value!!.update(i, 100)
        }
        processState.postValue(ProcessState.FINISHED)
    }

    override fun cancel() {
        TODO("Not yet implemented")
    }
}