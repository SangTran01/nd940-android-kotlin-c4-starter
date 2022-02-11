package com.udacity.project4.locationreminders.data.local

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
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - insert a task
        val rem2 = ReminderDTO(
            "Title2", "Description2",
            "Loc2", 1.0, 2.0
        )
        database.reminderDao().saveReminder(rem2)

        // WHEN - Get the task by id from the database
        val loaded = database.reminderDao().getReminderById(rem2.id)

        loaded?.let {
            assertThat(loaded.id, `is`(rem2.id))
            assertThat(loaded.title, `is`(rem2.title))
            assertThat(loaded.description, `is`(rem2.description))
        }
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        val rem1 = ReminderDTO(
            "Title1", "Description1",
            "Loc1", 1.0, 2.0
        )
        val rem2 = ReminderDTO(
            "Title2", "Description2",
            "Loc2", 1.0, 2.0
        )

        database.reminderDao().saveReminder(rem1)
        database.reminderDao().saveReminder(rem2)

        database.reminderDao().deleteAllReminders()

        val size = database.reminderDao().getReminders().count()

        assertThat(size, `is`(0))
    }
}