package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeAndroidTestRepository
import com.udacity.project4.locationreminders.data.local.LocalDB
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
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
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var repository: FakeAndroidTestRepository
    private lateinit var appContext: Application

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        stopKoin()
        val appModule = module {
            viewModel {
                RemindersListViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
//            single<ReminderDataSource> { RemindersLocalRepository(get()) }
//            single<ReminderDataSource> { FakeAndroidTestRepository() }
            // ReminderDataSource
            single<ReminderDataSource> { get<FakeAndroidTestRepository>() }
            // FakeAndroidTestRepository
            single<FakeAndroidTestRepository> { FakeAndroidTestRepository() }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(appModule))
        }
        repository = GlobalContext.get().koin.get()
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun example() {
        assertThat(1 + 1, `is`(2))
    }

    @Test
    fun reminderListFragment_DisplayNoReminders() = runBlockingTest {
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withText(R.string.no_data)).check(matches(isDisplayed()))
    }

    @Test
    fun reminderListFragment_DisplayReminders() = runBlockingTest {
        val reminder = ReminderDTO("title", "desc", "home", 1.0, 2.0)

        repository.saveReminder(reminder)

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withText("title")).check(matches(isDisplayed()))
        onView(withText("desc")).check(matches(isDisplayed()))
        onView(withText("home")).check(matches(isDisplayed()))
    }

    @Test
    fun reminderListFragment_clickToSaveReminderFragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
}