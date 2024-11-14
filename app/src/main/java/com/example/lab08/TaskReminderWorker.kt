package com.example.lab08

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val taskDescription = inputData.getString("task_description") ?: return Result.failure()
        val taskId = inputData.getInt("task_id", 0)

        NotificationHelper(applicationContext).showTaskReminder(
            Task(id = taskId, description = taskDescription)
        )

        return Result.success()
    }
}