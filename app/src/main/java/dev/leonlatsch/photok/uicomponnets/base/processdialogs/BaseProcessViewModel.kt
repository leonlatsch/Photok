/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.uicomponnets.base.processdialogs

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Abstract base for all processing view models.
 * Holds live data for [ProcessState].
 * Provides abstract functions called by the ui.
 *
 * @param T Type of elements to be processed
 *
 * @sine 1.0.0
 * @author Leon Latsch
 */
abstract class BaseProcessViewModel<T>(app: Application) : ObservableViewModel(app) {

    /**
     * List of [T].
     * Gets set automatically by fragment.
     * Gets processed in [processLoop].
     */
    lateinit var items: List<T>

    /**
     * The processing state should be checked every time in [processLoop].
     */
    @get:Bindable
    var processState: ProcessState = ProcessState.INITIALIZE
        set(value) {
            field = value
            notifyChange(BR.processState, value)
        }

    @get:Bindable
    var progressPercent: Int = 0
        set(value) {
            field = value
            notifyChange(BR.progressPercent, value)
        }


    @get:Bindable
    var current: Int = 0
        set(value) {
            field = value
            notifyChange(BR.current, value)
        }

    /**
     * The number of elements to get processed.
     * Gets set automatically.
     */
    @get:Bindable
    var elementsToProcess = 0
        set(value) {
            field = value
            notifyChange(BR.elementsToProcess, value)
        }

    /**
     * Indicates if failures occurred.
     * Gets evaluated by base DialogFragment to show warning. Should be set in [processItem] if an elements fails.
     */
    var failuresOccurred = false

    /**
     * Runs [preProcess], [processLoop] and [postProcess].
     * launched in [viewModelScope].
     */
    fun runProcessing() = viewModelScope.launch(Dispatchers.IO) {
        preProcess()
        processLoop()
        postProcess()
    }

    /**
     * Gets executed before [processLoop].
     */
    open suspend fun preProcess() {
        processState = ProcessState.PROCESSING
        updateProgress()
    }

    /**
     * Processing loop.
     * Calls [processItem].
     * Handles: Aborting and Updating progress.
     */
    private suspend fun processLoop() {
        for (item in items) {
            if (processState == ProcessState.ABORTED) {
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
        if (processState != ProcessState.ABORTED) {
            processState = ProcessState.FINISHED
        }
    }

    /**
     * Updates the state to [ProcessState.ABORTED].
     */
    open fun cancel() {
        processState = ProcessState.ABORTED
    }

    /**
     * Update the progress.
     */
    private fun itemProcessed() {
        current++
        updateProgress()
    }

    private fun updateProgress() {
        if (elementsToProcess == 0) {
            return
        }

        progressPercent = (current * 100) / elementsToProcess
    }

    /**
     * Sets [failuresOccurred] to true
     */
    fun failuresOccurred() {
        failuresOccurred = true
    }
}