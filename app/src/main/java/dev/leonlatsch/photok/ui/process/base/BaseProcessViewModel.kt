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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Abstract base for all processing view models.
 * Holds live data for [ProcessState] and [ProcessProgress].
 * Provides abstract functions called by the ui.
 *
 * @sine 1.0.0
 * @author Leon Latsch
 */
abstract class BaseProcessViewModel<T> : ViewModel() {

    /**
     * List of [T].
     * Gets set automatically by fragment.
     * Gets processed in [processLoop].
     */
    lateinit var items: List<T>

    /**
     * The processing state should be checked every time in [processLoop].
     */
    val processState: MutableLiveData<ProcessState> = MutableLiveData(ProcessState.INITIALIZE)

    /**
     * An [ProcessProgress] instance which is bound to the ui.
     */
    val progress = ProcessProgress()

    /**
     * Indicates if failures occurred.
     * Gets evaluated by base DialogFragment to show warning. Should be set in [processItem] if an elements fails.
     */
    var failuresOccurred = false

    /**
     * The current element index getting processed.
     * Used for [progress].
     */
    private var currentElement = 0

    /**
     * The number of elements to get processed.
     * Gets set automatically.
     */
    var elementsToProcess = 0

    /**
     * Runs [preProcess], [processLoop] and [postProcess].
     * launched in [viewModelScope].
     */
    fun runProcessing() = viewModelScope.launch {
        preProcess()
        processLoop()
        postProcess()
    }

    /**
     * Gets executed before [processLoop].
     */
    open suspend fun preProcess() {
        processState.postValue(ProcessState.PROCESSING)
        initProgress()
    }

    /**
     * Processing loop.
     * Calls [processItem].
     * Handles: Aborting and Updating progress.
     */
    private suspend fun processLoop() {
        for (item in items) {
            delay(1) // Delay ensure to always run a suspending call to update ui properly.
            if (processState.value == ProcessState.ABORTED) {
                return
            }

            processItem(item)
            itemProcessed()
        }
    }

    /**
     * Template method. Gets called by [processLoop].
     * Should implement the processing of one item.
     */
    abstract suspend fun processItem(item: T)

    /**
     * Get executed after [processLoop].
     */
    open suspend fun postProcess() {
        if (processState.value != ProcessState.ABORTED) {
            processState.postValue(ProcessState.FINISHED)
        }
    }

    /**
     * Updates the state to [ProcessState.ABORTED].
     */
    open fun cancel() {
        processState.postValue(ProcessState.ABORTED)
    }

    private fun initProgress() {
        progress.update(0, currentElement)
    }

    /**
     * Update the [progress] property.
     */
    private fun itemProcessed() {
        currentElement++
        progress.update(currentElement, elementsToProcess)
    }
}