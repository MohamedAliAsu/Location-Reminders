package com.udacity.project4.locationreminders.data.local

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO

@SmallTest
class RemindersDaoTest {
    @get:Rule
    val ier = InstantTaskExecutorRule()

    lateinit var db: RemindersDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java).allowMainThreadQueries().build()

    }

    @After
    fun cleanDb() {
        db.clearAllTables()
        db.close()
    }

    @Test
    fun deleteAllReminders_RemindersDeleted() = runBlockingTest {
        //GIVEN some reminders are in the database
        db.reminderDao().saveReminder(ReminderDTO("a", "b", "c", 0.0, 0.0))
        db.reminderDao().saveReminder(ReminderDTO("d", "e", "c", 0.0, 0.0))

        //WHEN deleting all reminders
        db.reminderDao().deleteAllReminders()
        //THEN no reminders in the database
        assertEquals(emptyList<ReminderDTO>(), db.reminderDao().getReminders())

    }

    //    TODO: Add testing implementation to the RemindersDao.kt
    @Test
    fun getReminderById() = runBlockingTest {
        //GIVEN added a reminder in the database
        val reminder = ReminderDTO("a", "b", "c", 0.0, 0.0)
        db.reminderDao().saveReminder(reminder)
        //WHEN getting the reminder by id from the db
        val result = db.reminderDao().getReminderById(reminder.id)
//THEN the retrieved reminder equals the inserted reminder
        assertThat(result as ReminderDTO, notNullValue())
        assertEquals(reminder.id, result.id)
        assertEquals(reminder.description, result.description)
        assertEquals(reminder.location, result.location)

    }

    @Test
    fun getReminders_getsAllReminders() = runBlockingTest{
        //GIVEN some reminders added to the db
        val reminders = listOf(ReminderDTO("a", "b", "c", 2.0, 1.0),
            ReminderDTO("a", "b", "c", 2.0, 1.0),
            ReminderDTO("a", "b", "c", 2.0, 1.0))
        for (r in reminders){
            db.reminderDao().saveReminder(r)
        }
        //WHEN retreiving reminders from the db
        //THEN the returned list is not empty
        assertThat(db.reminderDao().getReminders(),`is`(notNullValue()))
    }
}