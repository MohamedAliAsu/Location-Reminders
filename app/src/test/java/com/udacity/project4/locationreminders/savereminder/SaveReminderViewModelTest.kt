package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.CoroutineCustomRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi

import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertEquals


import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = intArrayOf(Build.VERSION_CODES.O_MR1))
class SaveReminderViewModelTest {
    @get:Rule
    val IER = InstantTaskExecutorRule()

    @get:Rule
    val coroutine = CoroutineCustomRule()

    lateinit var repo :FakeDataSource

    lateinit var vm :SaveReminderViewModel

    @Before
    fun setup(){
        repo = FakeDataSource()
        vm = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),repo)

    }
    @After
    fun cleanup(){
        stopKoin()
    }
    @Test
    fun emptyReminderTitle_UpdatesSnackBar(){
        //GIVEN creating reminder without title
        val r = ReminderDataItem("","a","a",2.0,0.0)
        //WHEN validating it with view model
        //THEN it returns false and changes snackbar value
        assertThat(vm.validateEnteredData(r)).isFalse()
        assertThat(vm.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }

    @Test
    fun emptyReminderLocation_UpdatesSnackBar(){
        //GIVEN creating reminder without location
        val r = ReminderDataItem("a","a","",2.0,0.0)
        //WHEN validating it with view model
        //THEN it returns false and changes snackbar value
        assertThat(vm.validateEnteredData(r)).isFalse()
        assertThat(vm.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }
    @Test
    fun saveReminderShowsLoading(){
        coroutine.pauseDispatcher()
        vm.saveReminder(ReminderDataItem("a","b","c",0.0,0.0))
        assertThat(vm.showLoading.getOrAwaitValue()).isTrue()
        coroutine.resumeDispatcher()
        assertThat(vm.showLoading.getOrAwaitValue()).isFalse()
    }



}