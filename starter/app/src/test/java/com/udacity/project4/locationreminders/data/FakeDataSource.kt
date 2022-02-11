package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.lang.Exception

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminders: MutableList<ReminderDTO>) : ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return try {
            Result.Success(reminders)
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders.firstOrNull { it.id == id }
        reminder ?: return Result.Error("Reminder not exist")
        return Result.Success(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}