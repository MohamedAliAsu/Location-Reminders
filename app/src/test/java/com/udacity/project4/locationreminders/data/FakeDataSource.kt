package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource (var reminders:MutableList<ReminderDTO> = mutableListOf()): ReminderDataSource {

    var shouldReturnError = false
    fun setError(b:Boolean){
        shouldReturnError =b
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if(shouldReturnError){
            Result.Error("unknownError")
        }else
        {
            return Result.Success(reminders)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders.find{
            it.id == id
        }
       return when{
           (shouldReturnError)-> Result.Error("unknown error")
           reminder!=null->Result.Success(reminder)
            else-> Result.Error("couldn't find reminder")
       }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}