package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var dataSource: FakeDataSource

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var app: Application
    @Before
    fun setUpSaveReminderViewModel() {
        app = ApplicationProvider.getApplicationContext()
        val reminderDataItem = mutableListOf<ReminderDTO>()
        dataSource = FakeDataSource(reminderDataItem)
        saveReminderViewModel =
            SaveReminderViewModel(app, dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun onClear_valuesAreNull() {
        saveReminderViewModel.reminderTitle.value = "test"
        saveReminderViewModel.onClear()
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), nullValue())
    }

    @Test
    fun saveReminder_returnItemInDatasource() {
        mainCoroutineRule.pauseDispatcher()

        val reminder = ReminderDataItem("meet up", "test desc",
            "Library", 1.0, 2.0)
        saveReminderViewModel.saveReminder(reminder)


        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))

        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`(app.getString(R.string.reminder_saved)))

    }

    @Test
    fun validateEnteredData_returnTrueFalse() {
        val reminder = ReminderDataItem(null, "test desc",
            "Library", 1.0, 2.0)
        val resultFalse = saveReminderViewModel.validateEnteredData(reminder)
        assertThat(resultFalse, `is`(false))

        val reminder2 = ReminderDataItem("meet up", "test desc",
            null, 1.0, 2.0)
        val resultFalse2 = saveReminderViewModel.validateEnteredData(reminder2)
        assertThat(resultFalse2, `is`(false))

        val reminder3 = ReminderDataItem("meet up", "test desc",
            "Library", 1.0, 2.0)
        val resultTrue = saveReminderViewModel.validateEnteredData(reminder3)
        assertThat(resultTrue, `is`(true))
    }
}