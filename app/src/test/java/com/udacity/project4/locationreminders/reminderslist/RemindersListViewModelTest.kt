package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.CoroutineCustomRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi

import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Assert.assertEquals

import org.junit.Before


import org.junit.Rule
import org.junit.Test




import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi

class RemindersListViewModelTest {

    @get:Rule
    var coroutineRule = CoroutineCustomRule()

    @get:Rule
    var IER = InstantTaskExecutorRule()

    lateinit var repo: FakeDataSource
    lateinit var vm: RemindersListViewModel


    @After
    fun cleanup() {
        stopKoin()
    }

    @Before
    fun setup() {
        //setting up repository and viewmodel
        repo = FakeDataSource()
        vm = RemindersListViewModel(ApplicationProvider.getApplicationContext(), repo)
    }


    //this is the check_loading test function
    @Test
    fun loadingReminders_ShowsLoading() {
        //GIVEN coroutine paused
        coroutineRule.pauseDispatcher()
        //WHEN loading reminders from the view model
        vm.loadReminders()
        //THEN loading shows
        assertEquals(true, vm.showLoading.getOrAwaitValue())
        //WHEN coroutine resumes
        coroutineRule.resumeDispatcher()
        //THEN loading doesn't show
        assertEquals(false, vm.showLoading.getOrAwaitValue())
    }

    //this is the shouldReturnError test function
    @Test
    fun shouldReturnError_changesSnackbarValue() {

        coroutineRule.pauseDispatcher()
        //GIVEN the data source has errors getting reminders
        repo.setError(true)
        //WHEN loading reminders

        vm.loadReminders()


        coroutineRule.resumeDispatcher()
        //THEN the snackbar shows the error retreived
        assertEquals("unknownError", vm.showSnackBar.getOrAwaitValue())
    }

    @Test
    fun remindersLoaded_reminderslistInsNotEmpty() = coroutineRule.runBlockingTest {
        //GIVEN repository has reminders
        repo.saveReminder(ReminderDTO("A", "B", "C", 2.0, 2.0))
        //WHEN loading reminders from the view model
        vm.loadReminders()
        //THEN reminderslist is not empty
        assertThat(vm.remindersList.getOrAwaitValue().isNotEmpty())
    }



}