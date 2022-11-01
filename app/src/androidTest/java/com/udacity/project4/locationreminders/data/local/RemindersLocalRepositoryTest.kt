package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    val ier = InstantTaskExecutorRule()

    lateinit var db: RemindersDatabase
    lateinit var localRepo: RemindersLocalRepository

    @Before
    fun setupRepo() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java).allowMainThreadQueries().build()
        localRepo = RemindersLocalRepository(db.reminderDao(), Dispatchers.Main)

    }

    @After
    fun tearDown() {
        db.clearAllTables()
        db.close()

    }

    @Test
    fun deletingReminders_EmptyList() = runBlocking {
        //GIVEN saving the reminder to the repository

        val r = ReminderDTO("a", "b", "c", 0.0, 0.0)
        localRepo.saveReminder(r)
        //WHEN deleting all reminders
        localRepo.deleteAllReminders()
        val result = localRepo.getReminders()
        //THEN we can't retreive the reminder we saved by id
        assertEquals(true, result is Result.Success)
        result as Result.Success
        assertThat(result.data, `is`(emptyList()))


    }

    @Test
    fun getReminderByIdAfterDeleting_returnsError() = runBlocking {
        //GIVEN saving reminder to the repository
        val r = ReminderDTO("a", "b", "c", 0.0, 0.0)
        localRepo.saveReminder(r)
        //WHEN deleting all reminders from the repo

        localRepo.deleteAllReminders()
        val result = localRepo.getReminder(r.id)
        //THEN we get an error when getting the reminder by id
        assertEquals(true, result is Result.Error)
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))

    }
    @Test
    fun saveReminder_thenGetItById()=runBlocking{
        //GIVEN saving reminder to the repository
        val r = ReminderDTO("aa","bb","cc",11.0,22.1)
        localRepo.saveReminder(r)
        //WHEN retreiving reminder by the id of the previous one
        val result = localRepo.getReminder(r.id) as? Result.Success
        //THEN the resulting reminder equals the inserted one
        assertThat(result is Result.Success,`is`(true))


        result as Result.Success

        assertEquals(r,result.data)
    }
}