package com.udacity.project4

import androidx.test.espresso.idling.CountingIdlingResource

object EspressoIdling {
    @JvmField
    val countingIdlingResource = CountingIdlingResource("GLOBAL")
    fun increment(){
        countingIdlingResource.increment()
    }
    fun decrement(){
        if(!countingIdlingResource.isIdleNow)
        {
            countingIdlingResource.decrement()
        }
    }


}
inline fun <R> wrapEspressoIdlingResource(function: ()->R):R{
    EspressoIdling.increment()

    return try{
        function()
    }finally {
        EspressoIdling.decrement()
    }
}