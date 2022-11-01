package com.udacity.project4.locationreminders.reminderslist

import android.app.Application

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R


import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext


import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    @get:Rule
    val ier = InstantTaskExecutorRule()

    lateinit var repo: ReminderDataSource
    lateinit var app: Application

    @Before
    fun initialize() {
        stopKoin()
        app = getApplicationContext()

        val modulles = module {


            viewModel {
                RemindersListViewModel(
                    app,
                    get() as ReminderDataSource)
            }

            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(getApplicationContext()) }
        }

        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(modulles))
        }

        repo = GlobalContext.get().koin.get()


        //start with a clean repository

        runBlocking {
            repo.deleteAllReminders()
        }

    }

    @Test
    fun reminders_AreDisplayedInFragment() {
        val r = ReminderDTO("a", "b", "c", 0.0, 0.0)
        runBlocking {
            repo.saveReminder(r)
        }
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.reminderssRecyclerView)).perform(RecyclerViewActions.scrollTo<ViewHolder>(
            hasDescendant(withText(r.title))))

    }

    @Test
    fun clickingAddReminder_navigateToSaveFragment() {
        val mocknav = mock(NavController::class.java)
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment { frag ->
            Navigation.setViewNavController(frag.view!!, mocknav)
        }
        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(mocknav).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun onNoReminders_NoDataDisplayedInUI() {
        runBlocking {
            repo.saveReminder(ReminderDTO("a", "b", "c", 0.0, 0.0))
            repo.deleteAllReminders()
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        }
    }
}