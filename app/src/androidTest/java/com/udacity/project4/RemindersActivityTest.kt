package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.util.monitorActivity
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Rule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    lateinit var idlingRes: DataBindingIdlingResource



    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
        idlingRes = DataBindingIdlingResource()
        IdlingRegistry.getInstance().register(EspressoIdling.countingIdlingResource)
        IdlingRegistry.getInstance().register(idlingRes)
    }


    @After
    fun unregisterResources() {
        IdlingRegistry.getInstance().apply {
            unregister(EspressoIdling.countingIdlingResource)
            unregister(idlingRes)
        }

    }
    @Test
    fun saveReminderWithNoLocation_ShowsSnackError(){
        val actitySenario = ActivityScenario.launch(RemindersActivity::class.java)
        idlingRes.monitorActivity(actitySenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("title"))
        closeSoftKeyboard()

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText(appContext.getString(R.string.err_select_location))).check(matches( isDisplayed()))
        actitySenario.close()
    }
    @Test
    fun emptyReminderTitle_showsSnackbarError(){
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        idlingRes.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())


        onView(withText(appContext.getString(R.string.err_enter_title))).check(matches(isDisplayed()))
        scenario.close()
    }
    @Test
    fun savingCompleteReminder_ToastShowsSaved(){
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        idlingRes.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("My Home"))
        onView(withId(R.id.reminderDescription)).perform(typeText("moving next week"))
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.confirm)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        var activity:Activity? = null
        scenario.onActivity {
            activity = it
        }
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(activity?.window?.decorView)))).check(
            matches(isDisplayed()))
        scenario.close()

    }
}
