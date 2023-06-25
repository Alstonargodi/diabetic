package com.example.td_test_2.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.td_test_2.R
import com.example.td_test_2.database.Repository
import com.example.td_test_2.database.entity.task.TaskEntity
import com.example.td_test_2.database.room.DbConfig
import com.example.td_test_2.presentasion.mainactivity.MainActivity
import com.example.td_test_2.reminder.TaskReminder.Companion.ID_REPEATING
import java.util.Calendar
import java.util.concurrent.Executors

class TaskReminderBroadcast() : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Executors.newSingleThreadExecutor().execute {
            val task = Repository(DbConfig.setDatabase(context)).readTodayTask()
            if (task.isNotEmpty()) {
                showAlarmNotification(context, task)
            }
        }
    }

    fun setDailyReminder(context: Context, interval: Long) {
        val alarManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderBroadcast::class.java)

        //start reminder from 5 am
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 5)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ID_REPEATING,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        alarManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            interval,
            pendingIntent
        )
    }

    private fun showAlarmNotification(context: Context, data: List<TaskEntity>) {
        data.forEach {
            val notificationStyle = NotificationCompat.InboxStyle()
            val notificationFormat = context.resources.getString(R.string.notification_format)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val intent = Intent(context, MainActivity::class.java)

            val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            data.forEach { value ->
                val task = "${value.startTime} \n${value.title}"
                notificationStyle.addLine(task)
            }

            val builder = NotificationCompat.Builder(context, NOTIFICATION_Channel_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle("Kegiatan Hari ini")
                .setSmallIcon(R.drawable.baseline_access_alarm_24)
                .setStyle(notificationStyle)
                .setColor(ContextCompat.getColor(context, android.R.color.transparent))
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_Channel_ID,
                    NOTIFICATION_Channel_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                )

                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
                builder.setChannelId(NOTIFICATION_Channel_ID)
                notificationManager.createNotificationChannel(channel)
            }

            val notification = builder.build()
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    fun cancelAlarm(context: Context) {
        val alarManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderBroadcast::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ID_REPEATING,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent.cancel()
        alarManager.cancel(pendingIntent)
    }
    companion object {
        const val ID_REPEATING = 101
        const val NOTIFICATION_ID = 201
        const val NOTIFICATION_Channel_ID = "Repeat_Notification"
        const val NOTIFICATION_Channel_NAME = "Repeat_task"
    }
}