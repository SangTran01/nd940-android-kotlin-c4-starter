package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.gms.tasks.Task
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.*
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private val rem1 = ReminderDTO("Title1", "Description1",
        "Loc1", 1.0,2.0)


    // Class under test
    private lateinit var repository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        repository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun saveReminder_test() = runBlocking {
        repository.saveReminder(rem1)

        val remindersList = repository.getReminders()
        remindersList as Result.Success

        assertThat(remindersList.data[0].id, `is`(rem1.id))
        assertThat(remindersList.data[0].title, `is`(rem1.title))
        assertThat(remindersList.data[0].description, `is`(rem1.description))
        assertThat(remindersList.data[0].latitude, `is`(rem1.latitude))
        assertThat(remindersList.data[0].longitude, `is`(rem1.longitude))
        assertThat(remindersList.data[0].location, `is`(rem1.location))
    }

    @Test
    fun saveReminder_returnEmpty() = runBlocking {
        repository.saveReminder(rem1)
        val reminderListWithData = repository.getReminders() as Result.Success
        repository.deleteAllReminders()
        val remindersListAfterClear = repository.getReminders() as Result.Success

        assertThat(reminderListWithData.data.size, `is`(1))
        assertThat(remindersListAfterClear.data, `is`(emptyList()))
    }

    @Test
    fun getReminder_returnsError() = runBlocking {
        val gotData = repository.getReminder("1")

        assertThat(gotData, `is`(not(Result.Success(gotData))))
        assertThat(gotData, `is`(Result.Error("Reminder not found!")))
    }
}