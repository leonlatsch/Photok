package dev.leonlatsch.photok.base

import androidx.lifecycle.MutableLiveData

/**
 * Wrapper for [MutableLiveData] with Generic Type of Int.
 *
 * @since 1.0.0
 *
 */
class NumberLiveData : MutableLiveData<Int> {

    constructor() : super(0)

    constructor(initialValue: Int) : super(initialValue)

    /**
     * Increment the value by 1.
     */
    fun increment() {
        increment(1)
    }

    /**
     * Increment the value by [amount]
     */
    fun increment(amount: Int) = postValue(value!!.plus(amount))
}