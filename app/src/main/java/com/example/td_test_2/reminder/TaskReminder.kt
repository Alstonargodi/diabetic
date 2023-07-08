package com.example.td_test_2.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.td_test_2.R
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.presentasion.mainactivity.MainActivity

class TaskReminder(
    val context : Context,
    params : WorkerParameters,
) : Worker(context,params){
    private val repo = Repository(DbConfig.setDatabase(context))
    @RequiresApi(Build.VERSION_CODES.O)
    private val todayTask = repo.readTodayTaskList()

    private val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_ID,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        //TODO 14
        val notificationPreference = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val notification = notificationPreference.getBoolean(applicationContext.getString(R.string.pref_key_notify), false)

        if (notification){
            val notificationStyle = NotificationCompat.InboxStyle()
            val notificationFormat = context.resources.getString(R.string.notification_format)
            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            todayTask.forEach { todoLocal ->
                val task = String.format(
                    notificationFormat,
                    todoLocal.startTime,
                    todoLocal.endTime,
                    todoLocal.title)
                notificationStyle.addLine(task)
            }

            val notificationBuilder : NotificationCompat.Builder = NotificationCompat.Builder(applicationContext)
                .setContentIntent(pendingIntent)
                .setContentTitle("Today task")
                .setStyle(notificationStyle)
                .setColor(ContextCompat.getColor(context,android.R.color.transparent))
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channel = NotificationChannel(
                    NOTIFICATION_Channel_ID,
                    NOTIFICATION_Channel_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                )

                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
                notificationBuilder.setChannelId(NOTIFICATION_Channel_ID)
                notificationManager.createNotificationChannel(channel)
            }
            val currentNotification = notificationBuilder.build()
            notificationManager.notify(NOTIFICATION_ID,currentNotification)
        }

        return Result.success()
    }

    companion object{
        const val ID_REPEATING = 101
        const val NOTIFICATION_ID = 201
        const val NOTIFICATION_Channel_ID = "Repeat_Notification"
        const val NOTIFICATION_Channel_NAME = "Repeat_task"
    }
}