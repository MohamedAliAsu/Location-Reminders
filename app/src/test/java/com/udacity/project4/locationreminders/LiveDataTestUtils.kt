package com.udacity.project4.locationreminders

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveData<T>.getOrAwaitValue(
    t: Long = 2,
    tUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserving: () -> Unit = {}
): T {
    val latch = CountDownLatch(1)


    var data: T? = null


    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            //Todo i was stuck here
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)

    try {
        afterObserving.invoke()

        if (!latch.await(t, tUnit)) {
            throw TimeoutException("LiveData value is not set yet")
        }

    } finally {
        this.removeObserver(observer)
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}